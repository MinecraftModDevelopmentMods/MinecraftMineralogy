package zone.moddev.mc.mineralogy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import zone.moddev.mc.mineralogy.documentation.DocumentationExporter;
import zone.moddev.mc.mineralogy.migration.LegacyMineralogy6ConfigMigrator;
import zone.moddev.mc.mineralogy.migration.LegacyOreConfigMigrator;

import java.io.File;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ConfigurationAndMigrationTest {
    @Test
    public void cleanConfigContainsOnlyContentOptionsAndSafeDefaults() throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-clean-config");
        MineralogyConfig.load(directory);
        Path config = directory.resolve(MineralogyConfig.FILE_NAME);
        String text = new String(Files.readAllBytes(config), StandardCharsets.UTF_8);
        assertTrue(text.contains("[options]"));
        assertTrue(text.contains("COBBLESTONE_EQUIVILENT = true"));
        assertTrue(text.contains("GROUP_TABS_BY_TYPE = false"));
        assertTrue(text.contains("ENABLE_DRYWALLS = true"));
        assertFalse(text.contains("world-gen"));
        assertFalse(text.contains("PLACE_MINERALOGY_ROCK"));
        assertTrue(MineralogyConfig.makeRockCobblestoneEquivilent());
        assertFalse(MineralogyConfig.groupCreativeTabItemsByType());
        assertTrue(MineralogyConfig.contentPolicy().drywallsEnabled());
        assertTrue(MineralogyConfig.contentPolicy().rockSaltLampsEnabled());
        assertTrue(MineralogyConfig.contentPolicy().mineralDustsEnabled());
        assertTrue(MineralogyConfig.contentPolicy().mineralFertilizerEnabled());
    }

    @Test
    public void existingConfigIsReadWithoutBeingRewritten() throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-existing-config");
        Path config = directory.resolve(MineralogyConfig.FILE_NAME);
        byte[] original = ("# retained legacy file\r\n[options]\r\n"
                + "COBBLESTONE_EQUIVILENT = false\r\nGROUP_TABS_BY_TYPE = true\r\n"
                + "ENABLE_DRYWALLS = false\r\nENABLE_ROCK_SALT_LAMPS = false\r\n"
                + "ENABLE_MINERAL_DUSTS = false\r\nENABLE_MINERAL_FERTILIZER = false\r\n"
                + "[world-gen]\r\nGEOLOGY_MODE = \"GEOME\"\r\n").getBytes(StandardCharsets.UTF_8);
        Files.write(config, original);
        MineralogyConfig.load(directory);
        assertArrayEquals(original, Files.readAllBytes(config));
        assertFalse(MineralogyConfig.makeRockCobblestoneEquivilent());
        assertTrue(MineralogyConfig.groupCreativeTabItemsByType());
        assertFalse(MineralogyConfig.contentPolicy().drywallsEnabled());
        assertFalse(MineralogyConfig.contentPolicy().rockSaltLampsEnabled());
        assertFalse(MineralogyConfig.contentPolicy().mineralDustsEnabled());
        assertFalse(MineralogyConfig.contentPolicy().mineralFertilizerEnabled());
        assertFalse(MineralogyConfig.isCreativeVisible("drywall_black"));
        assertFalse(MineralogyConfig.isCreativeVisible("sulfur_dust"));
        assertFalse(MineralogyConfig.isCreativeVisible("mineral_fertilizer"));
    }

    @Test
    public void missingMineralogySixKeysUseEnabledDefaultsWithoutRewriting() throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-old-content-config");
        Path config = directory.resolve(MineralogyConfig.FILE_NAME);
        byte[] original = ("[options]\nDROP_COBBLESTONE = true # retained comment\n")
                .getBytes(StandardCharsets.UTF_8);
        Files.write(config, original);
        MineralogyConfig.load(directory);
        assertArrayEquals(original, Files.readAllBytes(config));
        assertTrue(MineralogyConfig.dropCobblestone());
        assertTrue(MineralogyConfig.contentPolicy().drywallsEnabled());
        assertTrue(MineralogyConfig.contentPolicy().rockSaltLampsEnabled());
        assertTrue(MineralogyConfig.contentPolicy().mineralDustsEnabled());
        assertTrue(MineralogyConfig.contentPolicy().mineralFertilizerEnabled());
        assertFalse(MineralogyConfig.groupCreativeTabItemsByType());
    }

    @Test
    public void issue121SwitchesRemainIndependent() throws Exception {
        String[] keys = { ContentPolicy.ENABLE_DRYWALLS, ContentPolicy.ENABLE_ROCK_SALT_LAMPS,
                ContentPolicy.ENABLE_MINERAL_DUSTS, ContentPolicy.ENABLE_MINERAL_FERTILIZER };
        String[] hidden = { "drywall_black", "rocksaltlamp", "sulfur_dust", "mineral_fertilizer" };
        for (int selected = 0; selected < keys.length; selected++) {
            Path directory = Files.createTempDirectory("mineralogy-policy-" + selected);
            StringBuilder config = new StringBuilder("[options]\n");
            for (int index = 0; index < keys.length; index++) {
                config.append(keys[index]).append(" = ").append(index != selected).append('\n');
            }
            Files.write(directory.resolve(MineralogyConfig.FILE_NAME),
                    config.toString().getBytes(StandardCharsets.UTF_8));
            MineralogyConfig.load(directory);
            for (int index = 0; index < hidden.length; index++) {
                assertEquals(keys[selected] + " must be independent for " + hidden[index],
                        index != selected, MineralogyConfig.isCreativeVisible(hidden[index]));
            }
        }
    }

    @Test
    public void independentOreMigrationConvertsExclusiveMaximumAndIsIdempotent() throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-ore-migration");
        Path config = directory.resolve(MineralogyConfig.FILE_NAME);
        byte[] source = ("[ores.sulfur_ore]\nminY = 7\nmaxY = 50\nfrequency = 2.5\nquantity = 9\n"
                + "[ores.phosphorous_ore]\nminY = 16\nmaxY = 64\nfrequency = 1.0\nquantity = 16\n"
                + "[ores.nitrate_ore]\nminY = 5\nmaxY = 40\nfrequency = nope\nquantity = 12\n")
                .getBytes(StandardCharsets.UTF_8);
        Files.write(config, source);
        LegacyOreConfigMigrator.migrate(config, null);
        Path override = directory.resolve(LegacyOreConfigMigrator.OVERRIDE_FILE);
        Path report = directory.resolve(LegacyOreConfigMigrator.REPORT_FILE);
        assertTrue(Files.isRegularFile(override));
        assertTrue(Files.isRegularFile(report));
        JsonObject root = parse(override.toFile());
        JsonObject sulfur = root.getAsJsonObject("ores")
                .getAsJsonObject("mineralogy:ore/mineralogy/sulfur_ore")
                .getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
        assertEquals(7, sulfur.get("min_y").getAsInt());
        assertEquals(49, sulfur.get("max_y").getAsInt());
        assertEquals(2.5D, sulfur.get("frequency").getAsDouble(), 0.0D);
        assertEquals(9, sulfur.get("quantity").getAsInt());
        String reportText = new String(Files.readAllBytes(report), StandardCharsets.UTF_8);
        assertTrue(reportText.contains("phosphorous_ore.result=default"));
        assertTrue(reportText.contains("nitrate_ore.result=default-invalid"));
        assertArrayEquals(source, Files.readAllBytes(config));

        byte[] firstOverride = Files.readAllBytes(override);
        byte[] firstReport = Files.readAllBytes(report);
        LegacyOreConfigMigrator.migrate(config, null);
        assertArrayEquals(firstOverride, Files.readAllBytes(override));
        assertArrayEquals(firstReport, Files.readAllBytes(report));
    }

    @Test
    public void establishedOverridesRemainAuthoritative() throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-established-override");
        Path config = directory.resolve(MineralogyConfig.FILE_NAME);
        Files.write(config, ("[ores.sulphur_ore]\nminY=1\nmaxY=20\nfrequency=3\nquantity=4\n")
                .getBytes(StandardCharsets.UTF_8));
        Path override = directory.resolve(LegacyOreConfigMigrator.OVERRIDE_FILE);
        byte[] established = "{\"established\":true}\n".getBytes(StandardCharsets.UTF_8);
        Files.write(override, established);
        LegacyOreConfigMigrator.migrate(config, null);
        assertArrayEquals(established, Files.readAllBytes(override));
        String report = new String(Files.readAllBytes(directory.resolve(
                LegacyOreConfigMigrator.REPORT_FILE)), StandardCharsets.UTF_8);
        assertTrue(report.contains("existing Mineralogy provider override is authoritative"));
    }

    @Test
    public void establishedOreSpawnGlobalConfigurationPreventsMigration() throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-established-global");
        Path config = directory.resolve(MineralogyConfig.FILE_NAME);
        Files.write(config, ("[ores.sulfur_ore]\nminY=1\nmaxY=20\nfrequency=3\nquantity=4\n")
                .getBytes(StandardCharsets.UTF_8));
        byte[] global = "{\"schema_version\":6}\n".getBytes(StandardCharsets.UTF_8);
        Files.write(directory.resolve(LegacyOreConfigMigrator.GLOBAL_FILE), global);
        LegacyOreConfigMigrator.migrate(config, null);
        assertFalse(Files.exists(directory.resolve(LegacyOreConfigMigrator.OVERRIDE_FILE)));
        assertArrayEquals(global, Files.readAllBytes(directory.resolve(LegacyOreConfigMigrator.GLOBAL_FILE)));
        assertTrue(new String(Files.readAllBytes(directory.resolve(LegacyOreConfigMigrator.REPORT_FILE)),
                StandardCharsets.UTF_8).contains("existing OreSpawn global configuration is authoritative"));
    }

    @Test
    public void nativeSulfurCategoryWinsHistoricalFallbackAndMaximum256Becomes255() throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-duplicate-sulfur");
        Path config = directory.resolve(MineralogyConfig.FILE_NAME);
        Files.write(config, ("[ores.sulphur_ore]\nminY=2\nmaxY=30\nfrequency=2\nquantity=3\n"
                + "[ores.sulfur_ore]\nminY=8\nmaxY=256\nfrequency=0\nquantity=7\n")
                .getBytes(StandardCharsets.UTF_8));
        LegacyOreConfigMigrator.migrate(config, null);
        JsonObject sulfur = parse(directory.resolve(LegacyOreConfigMigrator.OVERRIDE_FILE).toFile())
                .getAsJsonObject("ores").getAsJsonObject("mineralogy:ore/mineralogy/sulfur_ore")
                .getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
        assertEquals(8, sulfur.get("min_y").getAsInt());
        assertEquals(255, sulfur.get("max_y").getAsInt());
        assertEquals(0.0D, sulfur.get("frequency").getAsDouble(), 0.0D);
        assertEquals(7, sulfur.get("quantity").getAsInt());
    }

    @Test
    public void minecraft118OreBoundsPreserveNegativeMinimumAndExclusiveMaximum320()
            throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-118-ore-bounds");
        Path config = directory.resolve(MineralogyConfig.FILE_NAME);
        Files.write(config, ("[ores.sulfur_ore]\nminY=-64\nmaxY=320\nfrequency=2\nquantity=7\n")
                .getBytes(StandardCharsets.UTF_8));
        LegacyOreConfigMigrator.migrate(config, null);
        JsonObject sulfur = parse(directory.resolve(LegacyOreConfigMigrator.OVERRIDE_FILE).toFile())
                .getAsJsonObject("ores").getAsJsonObject("mineralogy:ore/mineralogy/sulfur_ore")
                .getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
        assertEquals(-64, sulfur.get("min_y").getAsInt());
        assertEquals(319, sulfur.get("max_y").getAsInt());
    }

    @Test
    public void allDefaultsProduceOnlyAByteStableReport() throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-default-ores");
        Path config = directory.resolve(MineralogyConfig.FILE_NAME);
        byte[] source = ("[ores.sulfur_ore]\nminY=16\nmaxY=64\nfrequency=1.0\nquantity=16\n"
                + "[ores.phosphorous_ore]\nminY=16\nmaxY=64\nfrequency=1.0\nquantity=16\n"
                + "[ores.nitrate_ore]\nminY=16\nmaxY=64\nfrequency=1.0\nquantity=16\n")
                .getBytes(StandardCharsets.UTF_8);
        Files.write(config, source);
        LegacyOreConfigMigrator.migrate(config, null);
        Path report = directory.resolve(LegacyOreConfigMigrator.REPORT_FILE);
        byte[] first = Files.readAllBytes(report);
        assertFalse(Files.exists(directory.resolve(LegacyOreConfigMigrator.OVERRIDE_FILE)));
        LegacyOreConfigMigrator.migrate(config, null);
        assertArrayEquals(first, Files.readAllBytes(report));
        assertArrayEquals(source, Files.readAllBytes(config));
    }

    @Test
    public void mineralogySixGlobalJsonUsesStableProviderIdsWithoutRewritingItsSource()
            throws Exception {
        Path directory = Files.createTempDirectory("mineralogy-six-json");
        JsonObject provider = parse(new File(
                "src/main/resources/data/mineralogy/orespawn/provider.json"));
        JsonObject legacy = legacyMineralogySixJson(provider, 4);
        JsonObject oldSulfur = legacy.getAsJsonObject("ores")
                .getAsJsonObject("mineralogy:sulfur_ore")
                .getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
        oldSulfur.addProperty("frequency", 2.75D);
        JsonObject vanilla = new JsonObject();
        vanilla.addProperty("enabled", false);
        legacy.getAsJsonObject("ores").add("minecraft:coal_ore", vanilla);

        Path source = directory.resolve(LegacyMineralogy6ConfigMigrator.LEGACY_GLOBAL_FILE);
        byte[] original = (legacy.toString() + "\r\n").getBytes(StandardCharsets.UTF_8);
        Files.write(source, original);

        assertTrue(LegacyMineralogy6ConfigMigrator.migrateGlobalConfig(directory, null));
        Path target = directory.resolve(LegacyMineralogy6ConfigMigrator.ORESPAWN_GLOBAL_FILE);
        JsonObject migrated = parse(target.toFile());
        assertEquals(6, migrated.get("schema_version").getAsInt());
        assertEquals(LegacyMineralogy6ConfigMigrator.LEGACY_GLOBAL_FILE,
                migrated.get("migrated_from").getAsString());
        assertEquals(32, migrated.getAsJsonObject("rocks").entrySet().size());
        assertEquals(4, migrated.getAsJsonObject("ores").entrySet().size());
        assertTrue(migrated.getAsJsonObject("rocks")
                .has("mineralogy:rock/minecraft/basalt"));
        assertFalse(migrated.getAsJsonObject("rocks").has("minecraft:basalt"));
        assertTrue(migrated.getAsJsonObject("ores")
                .has("mineralogy:ore/mineralogy/sulfur_ore"));
        assertFalse(migrated.getAsJsonObject("ores").has("mineralogy:sulfur_ore"));
        assertTrue(migrated.getAsJsonObject("ores").has("minecraft:coal_ore"));
        JsonObject sulfur = migrated.getAsJsonObject("ores")
                .getAsJsonObject("mineralogy:ore/mineralogy/sulfur_ore");
        assertEquals("mineralogy:sulfur_ore", sulfur.get("block").getAsString());
        assertEquals("mineralogy", sulfur.get("source_provider").getAsString());
        assertEquals(2.75D, sulfur.getAsJsonObject("dimensions")
                .getAsJsonObject("minecraft:overworld").get("frequency").getAsDouble(), 0.0D);
        assertArrayEquals(original, Files.readAllBytes(source));

        Path report = directory.resolve(LegacyMineralogy6ConfigMigrator.GLOBAL_REPORT_FILE);
        String reportText = new String(Files.readAllBytes(report), StandardCharsets.UTF_8);
        assertTrue(reportText.contains("canonicalized.rocks=32"));
        assertTrue(reportText.contains("canonicalized.ores=3"));
        byte[] firstTarget = Files.readAllBytes(target);
        byte[] firstReport = Files.readAllBytes(report);
        assertFalse(LegacyMineralogy6ConfigMigrator.migrateGlobalConfig(directory, null));
        assertArrayEquals(firstTarget, Files.readAllBytes(target));
        assertArrayEquals(firstReport, Files.readAllBytes(report));
        assertArrayEquals(original, Files.readAllBytes(source));
    }

    @Test
    public void mineralogySixWorldProfileIsCanonicalizedBeforeOreSpawnLoadsIt()
            throws Exception {
        Path world = Files.createTempDirectory("mineralogy-six-world");
        Path serverConfig = world.resolve("serverconfig");
        Files.createDirectories(serverConfig);
        JsonObject provider = parse(new File(
                "src/main/resources/data/mineralogy/orespawn/provider.json"));
        JsonObject legacy = legacyMineralogySixJson(provider, 3);
        Path source = serverConfig.resolve(LegacyMineralogy6ConfigMigrator.LEGACY_WORLD_FILE);
        byte[] original = (legacy.toString() + "\n").getBytes(StandardCharsets.UTF_8);
        Files.write(source, original);

        assertTrue(LegacyMineralogy6ConfigMigrator.migrateWorldProfile(world, null));
        Path target = serverConfig.resolve(LegacyMineralogy6ConfigMigrator.ORESPAWN_WORLD_FILE);
        JsonObject migrated = parse(target.toFile());
        assertEquals(5, migrated.get("schema_version").getAsInt());
        assertEquals(32, migrated.getAsJsonObject("rocks").entrySet().size());
        assertEquals(3, migrated.getAsJsonObject("ores").entrySet().size());
        assertTrue(migrated.getAsJsonObject("rocks")
                .has("mineralogy:rock/mineralogy/limestone"));
        assertTrue(Files.isRegularFile(serverConfig.resolve(
                LegacyMineralogy6ConfigMigrator.WORLD_REPORT_FILE)));
        assertArrayEquals(original, Files.readAllBytes(source));

        byte[] firstTarget = Files.readAllBytes(target);
        assertFalse(LegacyMineralogy6ConfigMigrator.migrateWorldProfile(world, null));
        assertArrayEquals(firstTarget, Files.readAllBytes(target));
        assertArrayEquals(original, Files.readAllBytes(source));
    }

    @Test
    public void establishedOreSpawnJsonAndMalformedMineralogyJsonAreNeverReplaced()
            throws Exception {
        Path establishedDirectory = Files.createTempDirectory("mineralogy-six-established");
        Files.write(establishedDirectory.resolve(
                LegacyMineralogy6ConfigMigrator.LEGACY_GLOBAL_FILE), "{}\n".getBytes(StandardCharsets.UTF_8));
        Path establishedTarget = establishedDirectory.resolve(
                LegacyMineralogy6ConfigMigrator.ORESPAWN_GLOBAL_FILE);
        byte[] established = "{\"established\":true}\n".getBytes(StandardCharsets.UTF_8);
        Files.write(establishedTarget, established);
        assertFalse(LegacyMineralogy6ConfigMigrator.migrateGlobalConfig(establishedDirectory, null));
        assertArrayEquals(established, Files.readAllBytes(establishedTarget));

        Path malformedDirectory = Files.createTempDirectory("mineralogy-six-malformed");
        byte[] malformed = "not json\r\n".getBytes(StandardCharsets.UTF_8);
        Path malformedSource = malformedDirectory.resolve(
                LegacyMineralogy6ConfigMigrator.LEGACY_GLOBAL_FILE);
        Files.write(malformedSource, malformed);
        assertFalse(LegacyMineralogy6ConfigMigrator.migrateGlobalConfig(malformedDirectory, null));
        assertFalse(Files.exists(malformedDirectory.resolve(
                LegacyMineralogy6ConfigMigrator.ORESPAWN_GLOBAL_FILE)));
        assertArrayEquals(malformed, Files.readAllBytes(malformedSource));
    }

    @Test
    public void documentationExportIsMissingOnlyAndCopiesExactProvider() throws Exception {
        Path output = Files.createTempDirectory("mineralogy-guide");
        assertEquals(7, DocumentationExporter.exportMissing(output));
        assertEquals(0, DocumentationExporter.exportMissing(output));
        Path readme = output.resolve("README.md");
        byte[] edited = "player edit\n".getBytes(StandardCharsets.UTF_8);
        Files.write(readme, edited);
        Files.delete(output.resolve("PLAYER_GUIDE.md"));
        assertEquals(1, DocumentationExporter.exportMissing(output));
        assertArrayEquals(edited, Files.readAllBytes(readme));
        assertArrayEquals(Files.readAllBytes(new File(
                "src/main/resources/data/mineralogy/orespawn/provider.json").toPath()),
                Files.readAllBytes(output.resolve("examples/mineralogy-provider.json")));
        try (Stream<Path> files = Files.walk(output)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }

    private static JsonObject parse(File file) throws Exception {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static JsonObject legacyMineralogySixJson(JsonObject provider, int schema) {
        JsonObject legacy = new JsonObject();
        legacy.addProperty("schema_version", schema);
        for (String sectionName : new String[] { "rocks", "ores" }) {
            JsonObject section = new JsonObject();
            for (java.util.Map.Entry<String, com.google.gson.JsonElement> entry
                    : provider.getAsJsonObject(sectionName).entrySet()) {
                JsonObject rule = entry.getValue().getAsJsonObject().deepCopy();
                String block = rule.get("block").getAsString();
                rule.remove("block");
                rule.remove("source_provider");
                section.add(block, rule);
            }
            legacy.add(sectionName, section);
        }
        return legacy;
    }
}
