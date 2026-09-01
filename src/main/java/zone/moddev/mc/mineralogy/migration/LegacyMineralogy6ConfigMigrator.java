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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.Logger;

/**
 * Imports Mineralogy 6's JSON geology configuration without duplicating the
 * stable rules supplied by Mineralogy's OreSpawn provider.
 *
 * <p>OreSpawn 4 can import the old files itself, but Mineralogy 6 used output
 * block IDs as rule IDs. Copying those IDs verbatim beside the provider's
 * stable IDs makes both ore rules active. This small pre-migration preserves
 * all user values while canonicalising only uniquely matching provider-owned
 * entries. Source files are never modified and established OreSpawn files are
 * always authoritative.</p>
 */
public final class LegacyMineralogy6ConfigMigrator {
    public static final String LEGACY_GLOBAL_FILE = "mineralogy-geomes.json";
    public static final String ORESPAWN_GLOBAL_FILE = "orespawn-worldgen.json";
    public static final String LEGACY_WORLD_FILE = "mineralogy-geology.json";
    public static final String ORESPAWN_WORLD_FILE = "orespawn-worldgen.json";
    public static final String GLOBAL_REPORT_FILE = "orespawn-migration/migration-report.txt";
    public static final String WORLD_REPORT_FILE = "mineralogy-orespawn-upgrade-report.txt";

    private static final String PROVIDER_RESOURCE = "data/mineralogy/orespawn/provider.json";
    private static final String PROVIDER_ID = "mineralogy";
    private static final int ORESPAWN_GLOBAL_SCHEMA = 6;
    private static final int ORESPAWN_WORLD_SCHEMA = 5;
    private static final String[] SECTIONS = { "rocks", "ores", "fluid_deposits" };
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private LegacyMineralogy6ConfigMigrator() {
    }

    public static boolean migrateGlobalConfig(Path configDirectory, Logger logger) {
        if (configDirectory == null) return false;
        Path directory = configDirectory.toAbsolutePath().normalize();
        return migrate(directory.resolve(LEGACY_GLOBAL_FILE),
                directory.resolve(ORESPAWN_GLOBAL_FILE),
                directory.resolve(GLOBAL_REPORT_FILE), "global", ORESPAWN_GLOBAL_SCHEMA, logger);
    }

    public static boolean migrateWorldProfile(Path worldRoot, Logger logger) {
        if (worldRoot == null) return false;
        Path directory = worldRoot.toAbsolutePath().normalize().resolve("serverconfig");
        return migrate(directory.resolve(LEGACY_WORLD_FILE),
                directory.resolve(ORESPAWN_WORLD_FILE),
                directory.resolve(WORLD_REPORT_FILE), "world", ORESPAWN_WORLD_SCHEMA, logger);
    }

    private static boolean migrate(Path source, Path target, Path report,
            String scope, int targetSchema, Logger logger) {
        if (!Files.isRegularFile(source) || Files.exists(target)) return false;

        try {
            JsonObject root = readObject(source);
            JsonObject provider = loadPackagedProvider();
            MigrationResult result = canonicalize(root, provider);
            root.addProperty("schema_version", targetSchema);
            root.addProperty("migrated_from", source.getFileName().toString());

            if (!writeAtomicallyIfAbsent(target, GSON.toJson(root) + "\n")) return false;
            writeAtomicallyIfAbsent(report, report(scope, source, target, result));
            info(logger, "Imported Mineralogy 6 {} geology settings into '{}' with {} rock, {} ore, "
                    + "and {} fluid-deposit rule IDs canonicalized; source retained unchanged",
                    scope, target, result.renamed.get("rocks"), result.renamed.get("ores"),
                    result.renamed.get("fluid_deposits"));
            return true;
        } catch (IOException | RuntimeException e) {
            warn(logger, "Could not pre-migrate Mineralogy 6 geology settings from '" + source
                    + "'; leaving OreSpawn's normal migration path in control", e);
            return false;
        }
    }

    private static MigrationResult canonicalize(JsonObject root, JsonObject provider) {
        MigrationResult result = new MigrationResult();
        for (String sectionName : SECTIONS) {
            JsonObject providerSection = object(provider, sectionName);
            if (!root.has(sectionName) || !root.get(sectionName).isJsonObject()
                    || providerSection.entrySet().isEmpty()) {
                continue;
            }

            JsonObject sourceSection = root.getAsJsonObject(sectionName);
            JsonObject targetSection = new JsonObject();
            Map<String, List<String>> idsByBlock = providerIdsByBlock(providerSection);

            // Explicit stable entries win if a partially migrated file also
            // contains the older block-named identity.
            for (Map.Entry<String, JsonElement> entry : sourceSection.entrySet()) {
                if (providerSection.has(entry.getKey())) {
                    targetSection.add(entry.getKey(), providerOwnedRule(entry.getValue(),
                            providerSection.get(entry.getKey())));
                }
            }

            for (Map.Entry<String, JsonElement> entry : sourceSection.entrySet()) {
                String oldId = entry.getKey();
                if (providerSection.has(oldId)) continue;
                String output = outputBlock(oldId, entry.getValue());
                List<String> matches = idsByBlock.get(output);
                if (matches == null || matches.size() != 1) {
                    targetSection.add(oldId, entry.getValue().deepCopy());
                    continue;
                }

                String stableId = matches.get(0);
                if (targetSection.has(stableId)) {
                    result.warnings.add(sectionName + ": skipped duplicate " + oldId
                            + " because stable rule " + stableId + " was already present");
                    continue;
                }
                targetSection.add(stableId, providerOwnedRule(entry.getValue(),
                        providerSection.get(stableId)));
                result.renamed.put(sectionName, result.renamed.get(sectionName) + 1);
            }
            root.add(sectionName, targetSection);
        }
        return result;
    }

    private static JsonElement providerOwnedRule(JsonElement legacy, JsonElement provider) {
        if (!legacy.isJsonObject() || !provider.isJsonObject()) return legacy.deepCopy();
        JsonObject result = legacy.getAsJsonObject().deepCopy();
        JsonObject providerRule = provider.getAsJsonObject();
        if (!result.has("block") && providerRule.has("block")) {
            result.add("block", providerRule.get("block").deepCopy());
        }
        result.addProperty("source_provider", PROVIDER_ID);
        return result;
    }

    private static Map<String, List<String>> providerIdsByBlock(JsonObject providerSection) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : providerSection.entrySet()) {
            String output = outputBlock("", entry.getValue());
            if (!output.isEmpty()) {
                result.computeIfAbsent(output, ignored -> new ArrayList<>()).add(entry.getKey());
            }
        }
        return result;
    }

    private static String outputBlock(String fallback, JsonElement value) {
        if (value != null && value.isJsonObject()) {
            JsonElement block = value.getAsJsonObject().get("block");
            if (block != null && block.isJsonPrimitive() && block.getAsJsonPrimitive().isString()) {
                return block.getAsString();
            }
        }
        return fallback;
    }

    private static String report(String scope, Path source, Path target, MigrationResult result) {
        StringBuilder text = new StringBuilder();
        text.append("Mineralogy 6 to OreSpawn 4 JSON migration report\n");
        text.append("scope=").append(scope).append('\n');
        text.append("source=").append(source.getFileName()).append('\n');
        text.append("target=").append(target.getFileName()).append('\n');
        text.append("result=migrated\n");
        text.append("source_retained_unchanged=true\n");
        for (String section : SECTIONS) {
            text.append("canonicalized.").append(section).append('=')
                    .append(result.renamed.get(section)).append('\n');
        }
        for (String warning : result.warnings) {
            text.append("warning=").append(warning).append('\n');
        }
        return text.toString();
    }

    private static JsonObject readObject(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) throw new IOException("JSON root is not an object");
            return parsed.getAsJsonObject();
        }
    }

    private static JsonObject loadPackagedProvider() throws IOException {
        InputStream stream = LegacyMineralogy6ConfigMigrator.class.getClassLoader()
                .getResourceAsStream(PROVIDER_RESOURCE);
        if (stream == null) throw new IOException("missing " + PROVIDER_RESOURCE);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) throw new IOException("provider root is not an object");
            return parsed.getAsJsonObject();
        }
    }

    private static JsonObject object(JsonObject root, String name) {
        return root.has(name) && root.get(name).isJsonObject()
                ? root.getAsJsonObject(name) : new JsonObject();
    }

    private static boolean writeAtomicallyIfAbsent(Path destination, String content)
            throws IOException {
        if (Files.exists(destination)) return false;
        Files.createDirectories(destination.getParent());
        Path temporary = destination.resolveSibling(destination.getFileName().toString() + ".tmp");
        Files.write(temporary, content.getBytes(StandardCharsets.UTF_8));
        try {
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, destination);
            }
            return true;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void info(Logger logger, String message, Object... values) {
        if (logger != null) logger.info(message, values);
    }

    private static void warn(Logger logger, String message, Throwable failure) {
        if (logger != null) logger.warn(message, failure);
    }

    private static final class MigrationResult {
        final Map<String, Integer> renamed = new LinkedHashMap<>();
        final List<String> warnings = new ArrayList<>();

        MigrationResult() {
            for (String section : SECTIONS) renamed.put(section, 0);
        }
    }
}
