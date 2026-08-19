package zone.moddev.mc.mineralogy.migration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** One-time, non-destructive migration of Mineralogy 3 ore settings to OreSpawn 4. */
public final class LegacyOreConfigMigrator {
    public static final String OVERRIDE_FILE = "mineralogy-orespawn.json";
    public static final String REPORT_FILE = "mineralogy-orespawn-upgrade-report.txt";
    public static final String GLOBAL_FILE = "orespawn-worldgen.json";
    private static final String PROVIDER_RESOURCE = "assets/mineralogy/orespawn/provider.json";
    private static final String PRIMARY_CATEGORY = "ores";
    private static final String FALLBACK_CATEGORY = "mineralogy ores";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final OreDefaults DEFAULTS = new OreDefaults(16, 64, 1.0D, 16);

    private LegacyOreConfigMigrator() {
    }

    public static void migrate(File mineralogyConfigFile, Logger logger) {
        if (mineralogyConfigFile == null || !mineralogyConfigFile.isFile()) {
            return;
        }

        Path configDirectory = mineralogyConfigFile.toPath().toAbsolutePath().getParent();
        if (configDirectory == null) {
            warn(logger, "Cannot locate the Mineralogy configuration directory; using packaged OreSpawn defaults.");
            return;
        }

        Path report = configDirectory.resolve(REPORT_FILE);
        if (Files.isRegularFile(report)) {
            return;
        }

        StringBuilder text = new StringBuilder();
        text.append("Mineralogy 6 / OreSpawn 4 ore migration report\n");
        text.append("source=mineralogy.cfg\n");

        Path override = configDirectory.resolve(OVERRIDE_FILE);
        Path global = configDirectory.resolve(GLOBAL_FILE);
        if (Files.isRegularFile(override)) {
            text.append("result=skipped\nreason=existing Mineralogy provider override is authoritative\n");
            writeAtomicallyIfAbsent(report, text.toString(), logger);
            return;
        }
        if (Files.isRegularFile(global)) {
            text.append("result=skipped\nreason=existing OreSpawn global configuration is authoritative\n");
            writeAtomicallyIfAbsent(report, text.toString(), logger);
            return;
        }

        Map<String, Map<String, String>> categories;
        try {
            categories = parseCategories(mineralogyConfigFile.toPath());
        } catch (IOException ex) {
            text.append("result=defaults\nwarning=unable to read legacy Mineralogy configuration\n");
            warn(logger, "Unable to read legacy Mineralogy ore settings; using packaged OreSpawn defaults: " + ex.getMessage());
            writeAtomicallyIfAbsent(report, text.toString(), logger);
            return;
        }

        String categoryName = categories.containsKey(PRIMARY_CATEGORY) ? PRIMARY_CATEGORY
                : categories.containsKey(FALLBACK_CATEGORY) ? FALLBACK_CATEGORY : null;
        text.append("category=").append(categoryName == null ? "none" : categoryName).append('\n');
        if (categoryName == null) {
            text.append("result=defaults\nreason=no legacy ore category found\n");
            writeAtomicallyIfAbsent(report, text.toString(), logger);
            return;
        }

        Map<String, String> values = categories.get(categoryName);
        Map<String, OreSettings> migrations = new LinkedHashMap<String, OreSettings>();
        inspectOre("sulphur_ore", "mineralogy:ore/mineralogy/sulfur_ore", values, migrations, text, logger);
        inspectOre("phosphorous_ore", "mineralogy:ore/mineralogy/phosphorous_ore", values, migrations, text, logger);
        inspectOre("nitrate_ore", "mineralogy:ore/mineralogy/nitrate_ore", values, migrations, text, logger);

        if (migrations.isEmpty()) {
            text.append("result=defaults\nreason=no valid non-default ore settings\n");
            writeAtomicallyIfAbsent(report, text.toString(), logger);
            return;
        }

        try {
            JsonObject provider = loadPackagedProvider();
            JsonObject ores = provider.getAsJsonObject("ores");
            for (Map.Entry<String, OreSettings> migration : migrations.entrySet()) {
                JsonObject ore = ores.getAsJsonObject(migration.getKey());
                JsonObject dimension = ore.getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
                OreSettings settings = migration.getValue();
                dimension.addProperty("min_y", settings.minY);
                dimension.addProperty("max_y", settings.maxYExclusive - 1);
                dimension.addProperty("frequency", settings.frequency);
                dimension.addProperty("quantity", settings.quantity);
            }
            writeAtomicallyIfAbsent(override, GSON.toJson(provider) + "\n", logger);
            text.append("result=migrated\noverride=").append(OVERRIDE_FILE).append('\n');
        } catch (Exception ex) {
            text.append("result=defaults\nwarning=unable to create provider override\n");
            warn(logger, "Unable to create the Mineralogy OreSpawn override; using packaged defaults: " + ex.getMessage());
        }
        writeAtomicallyIfAbsent(report, text.toString(), logger);
    }

    private static void inspectOre(String legacyName, String providerId, Map<String, String> values,
            Map<String, OreSettings> migrations, StringBuilder report, Logger logger) {
        String prefix = legacyName + ".";
        String minText = values.get(prefix + "miny");
        String maxText = values.get(prefix + "maxy");
        String frequencyText = values.get(prefix + "frequency");
        String quantityText = values.get(prefix + "quantity");
        report.append("ore.").append(legacyName).append(".source=")
                .append(valueOrMissing(minText)).append(',').append(valueOrMissing(maxText)).append(',')
                .append(valueOrMissing(frequencyText)).append(',').append(valueOrMissing(quantityText)).append('\n');

        OreSettings parsed = parseSettings(minText, maxText, frequencyText, quantityText);
        if (parsed == null) {
            report.append("ore.").append(legacyName).append(".result=default-invalid\n");
            warn(logger, "Invalid or incomplete legacy settings for " + legacyName + "; using packaged OreSpawn defaults.");
            return;
        }
        if (parsed.equals(DEFAULTS)) {
            report.append("ore.").append(legacyName).append(".result=default\n");
            return;
        }
        migrations.put(providerId, parsed);
        report.append("ore.").append(legacyName).append(".result=migrated")
                .append(" min_y=").append(parsed.minY)
                .append(" max_y=").append(parsed.maxYExclusive - 1)
                .append(" frequency=").append(formatDouble(parsed.frequency))
                .append(" quantity=").append(parsed.quantity).append('\n');
    }

    private static OreSettings parseSettings(String minText, String maxText, String frequencyText, String quantityText) {
        try {
            int minY = Integer.parseInt(minText);
            int maxY = Integer.parseInt(maxText);
            double frequency = Double.parseDouble(frequencyText);
            int quantity = Integer.parseInt(quantityText);
            if (minY < 1 || maxY > 255 || maxY <= minY || !Double.isFinite(frequency)
                    || frequency < 0.0D || frequency > 63.0D || quantity < 1 || quantity > 63) {
                return null;
            }
            return new OreSettings(minY, maxY, frequency, quantity);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static Map<String, Map<String, String>> parseCategories(Path source) throws IOException {
        Map<String, Map<String, String>> categories = new LinkedHashMap<String, Map<String, String>>();
        String current = null;
        for (String raw : Files.readAllLines(source, StandardCharsets.UTF_8)) {
            String line = raw.trim();
            if (line.endsWith("{") && line.indexOf(':') < 0) {
                String name = line.substring(0, line.length() - 1).trim();
                if (name.startsWith("\"") && name.endsWith("\"") && name.length() > 1) {
                    name = name.substring(1, name.length() - 1);
                }
                current = name.toLowerCase(Locale.ROOT);
                if (!categories.containsKey(current)) {
                    categories.put(current, new LinkedHashMap<String, String>());
                }
            } else if (line.equals("}")) {
                current = null;
            } else if (current != null && line.length() > 2 && line.charAt(1) == ':') {
                int equals = line.indexOf('=');
                if (equals > 2) {
                    String key = line.substring(2, equals).trim().toLowerCase(Locale.ROOT);
                    categories.get(current).put(key, line.substring(equals + 1).trim());
                }
            }
        }
        return categories;
    }

    private static JsonObject loadPackagedProvider() throws IOException {
        InputStream stream = LegacyOreConfigMigrator.class.getClassLoader().getResourceAsStream(PROVIDER_RESOURCE);
        if (stream == null) {
            throw new IOException("missing " + PROVIDER_RESOURCE);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            JsonElement parsed = new JsonParser().parse(reader);
            if (!parsed.isJsonObject()) {
                throw new IOException("provider root is not an object");
            }
            return parsed.getAsJsonObject();
        }
    }

    private static void writeAtomicallyIfAbsent(Path destination, String content, Logger logger) {
        if (Files.exists(destination)) {
            return;
        }
        Path temporary = destination.resolveSibling(destination.getFileName().toString() + ".tmp");
        try {
            Files.createDirectories(destination.getParent());
            Files.write(temporary, content.getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, destination);
            }
        } catch (IOException ex) {
            warn(logger, "Unable to write " + destination.getFileName() + ": " + ex.getMessage());
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                // Best effort cleanup only.
            }
        }
    }

    private static String valueOrMissing(String value) {
        return value == null ? "missing" : value;
    }

    private static String formatDouble(double value) {
        return Double.toString(value);
    }

    private static void warn(Logger logger, String message) {
        if (logger != null) {
            logger.warn(message);
        }
    }

    private static class OreSettings {
        final int minY;
        final int maxYExclusive;
        final double frequency;
        final int quantity;

        OreSettings(int minY, int maxYExclusive, double frequency, int quantity) {
            this.minY = minY;
            this.maxYExclusive = maxYExclusive;
            this.frequency = frequency;
            this.quantity = quantity;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof OreSettings)) {
                return false;
            }
            OreSettings that = (OreSettings) other;
            return minY == that.minY && maxYExclusive == that.maxYExclusive
                    && Double.compare(frequency, that.frequency) == 0 && quantity == that.quantity;
        }

        @Override
        public int hashCode() {
            long bits = Double.doubleToLongBits(frequency);
            int result = 31 * minY + maxYExclusive;
            result = 31 * result + (int) (bits ^ (bits >>> 32));
            return 31 * result + quantity;
        }
    }

    private static final class OreDefaults extends OreSettings {
        OreDefaults(int minY, int maxYExclusive, double frequency, int quantity) {
            super(minY, maxYExclusive, frequency, quantity);
        }
    }
}
