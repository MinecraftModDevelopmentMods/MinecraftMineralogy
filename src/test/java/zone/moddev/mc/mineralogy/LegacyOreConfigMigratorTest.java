package zone.moddev.mc.mineralogy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import zone.moddev.mc.mineralogy.migration.LegacyOreConfigMigrator;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;

import static org.junit.Assert.*;

public class LegacyOreConfigMigratorTest {
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void defaultsKeepPackagedProvider() throws Exception {
        File config = config(category("ores", defaults(), defaults(), defaults()));
        byte[] source = Files.readAllBytes(config.toPath());
        LegacyOreConfigMigrator.migrate(config, null);
        assertFalse(override(config).exists());
        assertArrayEquals(source, Files.readAllBytes(config.toPath()));
        assertTrue(report(config).contains("result=defaults"));
    }

    @Test
    public void validNonDefaultsPreserveValuesAndConvertExclusiveMax() throws Exception {
        File config = config(category("ores", values(20, 80, 2.5, 9), defaults(), defaults()));
        byte[] source = Files.readAllBytes(config.toPath());
        LegacyOreConfigMigrator.migrate(config, null);
        JsonObject sulfur = dimension(config, "mineralogy:ore/mineralogy/sulfur_ore");
        assertEquals(20, sulfur.get("min_y").getAsInt());
        assertEquals(79, sulfur.get("max_y").getAsInt());
        assertEquals(2.5D, sulfur.get("frequency").getAsDouble(), 0.0D);
        assertEquals(9, sulfur.get("quantity").getAsInt());
        assertArrayEquals(source, Files.readAllBytes(config.toPath()));
    }

    @Test
    public void native112SulfurKeyWinsOverHistoricalSulphurFallback() throws Exception {
        String source = "\"ores\" {\n"
                + properties("sulphur_ore", values(5, 30, 3, 7))
                + properties("sulfur_ore", values(20, 80, 2.5, 9))
                + properties("phosphorous_ore", defaults())
                + properties("nitrate_ore", defaults())
                + "}\n";
        File config = config(source);

        LegacyOreConfigMigrator.migrate(config, null);

        JsonObject sulfur = dimension(config, "mineralogy:ore/mineralogy/sulfur_ore");
        assertEquals(20, sulfur.get("min_y").getAsInt());
        assertEquals(79, sulfur.get("max_y").getAsInt());
        assertTrue(report(config).contains("ore.sulfur_ore.result=migrated"));
        assertFalse(report(config).contains("ore.sulphur_ore.source="));
    }

    @Test
    public void historicalSulphurKeyRemainsSupportedWhenNativeKeyIsAbsent() throws Exception {
        File config = config(category("ores", values(19, 70, 1.5, 8), defaults(), defaults()));

        LegacyOreConfigMigrator.migrate(config, null);

        JsonObject sulfur = dimension(config, "mineralogy:ore/mineralogy/sulfur_ore");
        assertEquals(19, sulfur.get("min_y").getAsInt());
        assertEquals(69, sulfur.get("max_y").getAsInt());
        assertTrue(report(config).contains("ore.sulphur_ore.result=migrated"));
    }

    @Test
    public void primaryCategoryWinsWhenBothLegacyNamesExist() throws Exception {
        String source = category("mineralogy ores", values(4, 30, 5, 6), defaults(), defaults())
                + category("ores", defaults(), values(21, 70, 3, 8), defaults());
        File config = config(source);
        LegacyOreConfigMigrator.migrate(config, null);
        assertEquals(16, dimension(config, "mineralogy:ore/mineralogy/sulfur_ore").get("min_y").getAsInt());
        assertEquals(21, dimension(config, "mineralogy:ore/mineralogy/phosphorous_ore").get("min_y").getAsInt());
        assertTrue(report(config).contains("category=ores"));
    }

    @Test
    public void historicalFallbackCategoryMigrates() throws Exception {
        File config = config(category("mineralogy ores", defaults(), defaults(), values(8, 40, 0.5, 7)));
        LegacyOreConfigMigrator.migrate(config, null);
        assertEquals(39, dimension(config, "mineralogy:ore/mineralogy/nitrate_ore").get("max_y").getAsInt());
        assertTrue(report(config).contains("category=mineralogy ores"));
    }

    @Test
    public void invalidOreFallsBackIndependentlyWhileValidOreMigrates() throws Exception {
        File config = config(category("ores", values(16, 64, 1, 0), values(12, 50, 4, 5), defaults()));
        LegacyOreConfigMigrator.migrate(config, null);
        assertEquals(16, dimension(config, "mineralogy:ore/mineralogy/sulfur_ore").get("quantity").getAsInt());
        assertEquals(12, dimension(config, "mineralogy:ore/mineralogy/phosphorous_ore").get("min_y").getAsInt());
        assertTrue(report(config).contains("ore.sulphur_ore.result=default-invalid"));
    }

    @Test
    public void zeroFrequencyIsRepresentableButZeroQuantityIsNot() throws Exception {
        File config = config(category("ores", values(16, 64, 0, 16), defaults(), values(16, 64, 1, 0)));
        LegacyOreConfigMigrator.migrate(config, null);
        assertEquals(0.0D, dimension(config, "mineralogy:ore/mineralogy/sulfur_ore").get("frequency").getAsDouble(), 0.0D);
        assertEquals(16, dimension(config, "mineralogy:ore/mineralogy/nitrate_ore").get("quantity").getAsInt());
    }

    @Test
    public void invalidRangesAndNonFiniteValuesKeepPackagedDefaults() throws Exception {
        File config = config(category("ores",
                values(0, 64, 1, 16), values(40, 40, 1, 16), values(16, 64, Double.NaN, 16)));
        LegacyOreConfigMigrator.migrate(config, null);
        assertFalse(override(config).exists());
        String report = report(config);
        assertTrue(report.contains("ore.sulphur_ore.result=default-invalid"));
        assertTrue(report.contains("ore.phosphorous_ore.result=default-invalid"));
        assertTrue(report.contains("ore.nitrate_ore.result=default-invalid"));
    }

    @Test
    public void establishedProviderOverrideIsNeverModified() throws Exception {
        File config = config(category("ores", values(20, 80, 2, 9), defaults(), defaults()));
        File override = override(config);
        Files.write(override.toPath(), "existing-provider\n".getBytes(StandardCharsets.UTF_8));
        LegacyOreConfigMigrator.migrate(config, null);
        assertEquals("existing-provider\n", new String(Files.readAllBytes(override.toPath()), StandardCharsets.UTF_8));
        assertTrue(report(config).contains("existing Mineralogy provider override is authoritative"));
    }

    @Test
    public void establishedGlobalConfigurationIsNeverModified() throws Exception {
        File config = config(category("ores", values(20, 80, 2, 9), defaults(), defaults()));
        File global = new File(config.getParentFile(), LegacyOreConfigMigrator.GLOBAL_FILE);
        Files.write(global.toPath(), "established-global\n".getBytes(StandardCharsets.UTF_8));
        LegacyOreConfigMigrator.migrate(config, null);
        assertFalse(override(config).exists());
        assertEquals("established-global\n", new String(Files.readAllBytes(global.toPath()), StandardCharsets.UTF_8));
        assertTrue(report(config).contains("existing OreSpawn global configuration is authoritative"));
    }

    @Test
    public void secondStartIsByteStableAndPerformsNoAdditionalMigration() throws Exception {
        File config = config(category("ores", values(20, 80, 2, 9), defaults(), defaults()));
        LegacyOreConfigMigrator.migrate(config, null);
        File override = override(config);
        File reportFile = reportFile(config);
        byte[] overrideBytes = Files.readAllBytes(override.toPath());
        byte[] reportBytes = Files.readAllBytes(reportFile.toPath());
        FileTime marker = FileTime.fromMillis(946684800000L);
        Files.setLastModifiedTime(override.toPath(), marker);
        Files.setLastModifiedTime(reportFile.toPath(), marker);

        LegacyOreConfigMigrator.migrate(config, null);

        assertArrayEquals(overrideBytes, Files.readAllBytes(override.toPath()));
        assertArrayEquals(reportBytes, Files.readAllBytes(reportFile.toPath()));
        assertEquals(marker, Files.getLastModifiedTime(override.toPath()));
        assertEquals(marker, Files.getLastModifiedTime(reportFile.toPath()));
    }

    private File config(String content) throws Exception {
        File directory = temporary.newFolder();
        File config = new File(directory, "mineralogy.cfg");
        Files.write(config.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return config;
    }

    private static String category(String name, OreValues sulfur, OreValues phosphorous, OreValues nitrate) {
        return "\"" + name + "\" {\n"
                + properties("sulphur_ore", sulfur)
                + properties("phosphorous_ore", phosphorous)
                + properties("nitrate_ore", nitrate)
                + "}\n";
    }

    private static String properties(String name, OreValues values) {
        return " I:" + name + ".minY=" + values.minY + "\n"
                + " I:" + name + ".maxY=" + values.maxY + "\n"
                + " D:" + name + ".frequency=" + values.frequency + "\n"
                + " I:" + name + ".quantity=" + values.quantity + "\n";
    }

    private static OreValues defaults() {
        return values(16, 64, 1, 16);
    }

    private static OreValues values(int minY, int maxY, double frequency, int quantity) {
        return new OreValues(minY, maxY, frequency, quantity);
    }

    private static File override(File config) {
        return new File(config.getParentFile(), LegacyOreConfigMigrator.OVERRIDE_FILE);
    }

    private static File reportFile(File config) {
        return new File(config.getParentFile(), LegacyOreConfigMigrator.REPORT_FILE);
    }

    private static String report(File config) throws Exception {
        return new String(Files.readAllBytes(reportFile(config).toPath()), StandardCharsets.UTF_8);
    }

    private static JsonObject dimension(File config, String oreId) throws Exception {
        JsonObject provider = new JsonParser().parse(new FileReader(override(config))).getAsJsonObject();
        return provider.getAsJsonObject("ores").getAsJsonObject(oreId)
                .getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
    }

    private static final class OreValues {
        final int minY;
        final int maxY;
        final double frequency;
        final int quantity;

        OreValues(int minY, int maxY, double frequency, int quantity) {
            this.minY = minY;
            this.maxY = maxY;
            this.frequency = frequency;
            this.quantity = quantity;
        }
    }
}
