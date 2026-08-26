package zone.moddev.mc.mineralogy.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.Logger;

import zone.moddev.mc.mineralogy.MineralogyConfig;

/** One-time, non-destructive migration of Mineralogy 5 ore settings to OreSpawn 4. */
public final class LegacyOreConfigMigrator {
    public static final String OVERRIDE_FILE = "mineralogy-orespawn.json";
    public static final String REPORT_FILE = "mineralogy-orespawn-upgrade-report.txt";
    public static final String GLOBAL_FILE = "orespawn-worldgen.json";
    private static final String PROVIDER_RESOURCE = "data/mineralogy/orespawn/provider.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final OreSettings DEFAULTS = new OreSettings(16, 64, 1.0D, 16);

    private LegacyOreConfigMigrator() {
    }

    public static void migrate(Path mineralogyConfigFile, Logger logger) {
        if (mineralogyConfigFile == null || !Files.isRegularFile(mineralogyConfigFile)) return;
        Path directory = mineralogyConfigFile.toAbsolutePath().getParent();
        if (directory == null) return;
        Path report = directory.resolve(REPORT_FILE);
        if (Files.isRegularFile(report)) return;

        StringBuilder text = new StringBuilder();
        text.append("Mineralogy 6 / OreSpawn 4 ore migration report\n");
        text.append("source=").append(MineralogyConfig.FILE_NAME).append('\n');
        Path override = directory.resolve(OVERRIDE_FILE);
        if (Files.isRegularFile(override)) {
            text.append("result=skipped\nreason=existing Mineralogy provider override is authoritative\n");
            writeAtomicallyIfAbsent(report, text.toString(), logger);
            return;
        }
        if (Files.isRegularFile(directory.resolve(GLOBAL_FILE))) {
            text.append("result=skipped\nreason=existing OreSpawn global configuration is authoritative\n");
            writeAtomicallyIfAbsent(report, text.toString(), logger);
            return;
        }

        Map<String, String> values = MineralogyConfig.readLegacyValues(mineralogyConfigFile);
        Map<String, OreSettings> migrations = new LinkedHashMap<>();
        inspect("sulfur_ore", "sulphur_ore", "mineralogy:ore/mineralogy/sulfur_ore",
                values, migrations, text, logger);
        inspect("phosphorous_ore", null, "mineralogy:ore/mineralogy/phosphorous_ore",
                values, migrations, text, logger);
        inspect("nitrate_ore", null, "mineralogy:ore/mineralogy/nitrate_ore",
                values, migrations, text, logger);
        if (migrations.isEmpty()) {
            text.append("result=defaults\nreason=no valid non-default ore settings\n");
            writeAtomicallyIfAbsent(report, text.toString(), logger);
            return;
        }

        try {
            JsonObject provider = loadPackagedProvider();
            JsonObject ores = provider.getAsJsonObject("ores");
            for (Map.Entry<String, OreSettings> entry : migrations.entrySet()) {
                JsonObject dimension = ores.getAsJsonObject(entry.getKey())
                        .getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
                OreSettings settings = entry.getValue();
                dimension.addProperty("min_y", settings.minY);
                dimension.addProperty("max_y", settings.maxYExclusive - 1);
                dimension.addProperty("frequency", settings.frequency);
                dimension.addProperty("quantity", settings.quantity);
            }
            writeAtomicallyIfAbsent(override, GSON.toJson(provider) + "\n", logger);
            text.append("result=migrated\noverride=").append(OVERRIDE_FILE).append('\n');
        } catch (Exception e) {
            text.append("result=defaults\nwarning=unable to create provider override\n");
            warn(logger, "Unable to create Mineralogy OreSpawn override; using packaged defaults", e);
        }
        writeAtomicallyIfAbsent(report, text.toString(), logger);
    }

    private static void inspect(String primary, String fallback, String providerId,
            Map<String, String> values, Map<String, OreSettings> migrations,
            StringBuilder report, Logger logger) {
        String selected = has(values, primary) ? primary : fallback != null && has(values, fallback) ? fallback : primary;
        String prefix = "ores." + selected.toLowerCase() + ".";
        String min = values.get(prefix + "miny");
        String max = values.get(prefix + "maxy");
        String frequency = values.get(prefix + "frequency");
        String quantity = values.get(prefix + "quantity");
        report.append("ore.").append(selected).append(".source=")
                .append(value(min)).append(',').append(value(max)).append(',')
                .append(value(frequency)).append(',').append(value(quantity)).append('\n');
        OreSettings parsed = parse(min, max, frequency, quantity);
        if (parsed == null) {
            report.append("ore.").append(selected).append(".result=default-invalid\n");
            if (min != null || max != null || frequency != null || quantity != null) {
                warn(logger, "Invalid or incomplete legacy settings for " + selected
                        + "; using packaged OreSpawn defaults", null);
            }
        } else if (parsed.equals(DEFAULTS)) {
            report.append("ore.").append(selected).append(".result=default\n");
        } else {
            migrations.put(providerId, parsed);
            report.append("ore.").append(selected).append(".result=migrated min_y=")
                    .append(parsed.minY).append(" max_y=").append(parsed.maxYExclusive - 1)
                    .append(" frequency=").append(parsed.frequency)
                    .append(" quantity=").append(parsed.quantity).append('\n');
        }
    }

    private static boolean has(Map<String, String> values, String ore) {
        String prefix = "ores." + ore.toLowerCase() + ".";
        return values.containsKey(prefix + "miny") || values.containsKey(prefix + "maxy")
                || values.containsKey(prefix + "frequency") || values.containsKey(prefix + "quantity");
    }

    private static OreSettings parse(String min, String max, String frequency, String quantity) {
        if (min == null && max == null && frequency == null && quantity == null) return DEFAULTS;
        try {
            int minY = Integer.parseInt(min);
            int maxY = Integer.parseInt(max);
            double attempts = Double.parseDouble(frequency);
            int size = Integer.parseInt(quantity);
            if (minY < 0 || maxY > 256 || maxY <= minY || !Double.isFinite(attempts)
                    || attempts < 0.0D || attempts > 64.0D || size < 1 || size > 64) return null;
            return new OreSettings(minY, maxY, attempts, size);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static JsonObject loadPackagedProvider() throws IOException {
        InputStream stream = LegacyOreConfigMigrator.class.getClassLoader().getResourceAsStream(PROVIDER_RESOURCE);
        if (stream == null) throw new IOException("missing " + PROVIDER_RESOURCE);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            JsonElement parsed = new JsonParser().parse(reader);
            if (!parsed.isJsonObject()) throw new IOException("provider root is not an object");
            return parsed.getAsJsonObject();
        }
    }

    private static void writeAtomicallyIfAbsent(Path destination, String content, Logger logger) {
        if (Files.exists(destination)) return;
        Path temporary = destination.resolveSibling(destination.getFileName().toString() + ".tmp");
        try {
            Files.createDirectories(destination.getParent());
            Files.write(temporary, content.getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, destination);
            }
        } catch (IOException e) {
            warn(logger, "Unable to write " + destination.getFileName(), e);
            try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
        }
    }

    private static String value(String value) { return value == null ? "missing" : value; }

    private static void warn(Logger logger, String message, Throwable failure) {
        if (logger == null) return;
        if (failure == null) logger.warn(message); else logger.warn(message, failure);
    }

    private static final class OreSettings {
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
        @Override public boolean equals(Object other) {
            if (!(other instanceof OreSettings)) return false;
            OreSettings that = (OreSettings) other;
            return minY == that.minY && maxYExclusive == that.maxYExclusive
                    && Double.compare(frequency, that.frequency) == 0 && quantity == that.quantity;
        }
        @Override public int hashCode() {
            long bits = Double.doubleToLongBits(frequency);
            int result = 31 * minY + maxYExclusive;
            result = 31 * result + (int) (bits ^ bits >>> 32);
            return 31 * result + quantity;
        }
    }
}
