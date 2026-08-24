package zone.moddev.mc.mineralogy;

import net.minecraftforge.common.config.Configuration;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContentPolicyTest {
    @Test
    public void allContentFamiliesDefaultToEnabled() {
        assertPolicy(ContentPolicy.defaults(), true, true, true, true);
    }

    @Test
    public void everyContentFamilyIsIndependent() {
        for (int mask = 0; mask < 16; mask++) {
            ContentPolicy policy = new ContentPolicy(
                    (mask & 1) != 0,
                    (mask & 2) != 0,
                    (mask & 4) != 0,
                    (mask & 8) != 0);
            assertPolicy(policy,
                    (mask & 1) != 0,
                    (mask & 2) != 0,
                    (mask & 4) != 0,
                    (mask & 8) != 0);
        }
    }

    @Test
    public void configurationDeclaresAllFourStableKeysWithEnabledDefaults() {
        Configuration config = mock(Configuration.class);
        when(config.getBoolean(anyString(), eq(ContentPolicy.CATEGORY), eq(true), anyString()))
                .thenReturn(true);
        assertPolicy(ContentPolicy.read(config), true, true, true, true);
        verify(config).getBoolean(eq(ContentPolicy.ENABLE_DRYWALLS),
                eq(ContentPolicy.CATEGORY), eq(true), anyString());
        verify(config).getBoolean(eq(ContentPolicy.ENABLE_ROCK_SALT_LAMPS),
                eq(ContentPolicy.CATEGORY), eq(true), anyString());
        verify(config).getBoolean(eq(ContentPolicy.ENABLE_MINERAL_DUSTS),
                eq(ContentPolicy.CATEGORY), eq(true), anyString());
        verify(config).getBoolean(eq(ContentPolicy.ENABLE_MINERAL_FERTILIZER),
                eq(ContentPolicy.CATEGORY), eq(true), anyString());
    }

    @Test
    public void readingContentOptionsNeverSavesTheConfiguration() {
        Configuration config = mock(Configuration.class);
        when(config.getBoolean(anyString(), eq(ContentPolicy.CATEGORY), eq(true), anyString()))
                .thenReturn(true);
        ContentPolicy.read(config);
        verify(config, never()).save();
    }

    @Test
    public void storedValuesAreReadIndependently() {
        final Map<String, Boolean> values = new HashMap<String, Boolean>();
        values.put(ContentPolicy.ENABLE_DRYWALLS, false);
        values.put(ContentPolicy.ENABLE_ROCK_SALT_LAMPS, true);
        values.put(ContentPolicy.ENABLE_MINERAL_DUSTS, false);
        values.put(ContentPolicy.ENABLE_MINERAL_FERTILIZER, true);

        Configuration config = mock(Configuration.class);
        when(config.getBoolean(anyString(), eq(ContentPolicy.CATEGORY), eq(true), anyString()))
                .thenAnswer(invocation -> values.get(invocation.getArgument(0)));
        assertPolicy(ContentPolicy.read(config), false, true, false, true);
    }

    @Test
    public void sourceKeepsPersistentContentContractsUnconditional() throws Exception {
        String blocks = source("src/main/java/zone/moddev/mc/mineralogy/init/Blocks.java");
        String items = source("src/main/java/zone/moddev/mc/mineralogy/init/Items.java");
        String ores = source("src/main/java/zone/moddev/mc/mineralogy/init/Ores.java");
        String registration = source("src/main/java/zone/moddev/mc/mineralogy/util/RegistrationHelper.java");

        assertTrue(blocks.contains("registerBlock(new RockSaltLamp(), \"rocksaltlamp\", \"lampRocksalt\")"));
        assertTrue(blocks.contains("registerBlock(new RockSaltStreetLamp(), \"rocksaltstreetlamp\", \"lampRocksaltStreet\", 16)"));
        assertTrue(blocks.contains("BlockItemPair drywall = RegistrationHelper.registerBlock(new DryWall"));
        assertTrue(ores.contains("addOre(Constants.PHOSPHOROUS"));
        assertTrue(items.contains("MineralogyRegistry.ItemsToRegister.put(Constants.DUST + oreDictionaryName, item)"));
        assertTrue(registration.contains("MineralogyRegistry.BlocksToRegister.put(oreDictionaryName, block)"));
        assertTrue(items.contains("MineralogyRegistry.ItemsToRegister.put(Constants.FERTILIZER, mineralFertilizer)"));
    }

    @Test
    public void sourceRoutesCreativeVisibilityThroughTheIndependentPolicies() throws Exception {
        String config = source("src/main/java/zone/moddev/mc/mineralogy/MineralogyConfig.java");
        String registration = source("src/main/java/zone/moddev/mc/mineralogy/util/RegistrationHelper.java");
        String items = source("src/main/java/zone/moddev/mc/mineralogy/init/Items.java");

        assertTrue(config.contains("name.startsWith(\"drywall_\")"));
        assertTrue(config.contains("\"rocksaltlamp\".equals(name) || \"rocksaltstreetlamp\".equals(name)"));
        assertTrue(config.contains("\"sulfur_dust\".equals(name)"));
        assertTrue(config.contains("\"phosphorous_block\".equals(name)"));
        assertTrue(config.contains("\"mineral_fertilizer\".equals(name)"));
        assertTrue(registration.contains("MineralogyConfig.isCreativeVisible(name)"));
        assertTrue(items.contains("mineralFertilizer.setCreativeTab(CreativeTabs.MATERIALS)"));
        assertTrue(items.contains("mineralFertilizer.setCreativeTab(null)"));
    }

    @Test
    public void craftingPoliciesUseTheForgeJsonConditionFactory() throws Exception {
        String factory = source("src/main/java/zone/moddev/mc/mineralogy/recipe/ConfigConditionFactory.java");
        String generator = source("scripts/generate-recipes.ps1");

        assertTrue(factory.contains("case ContentPolicy.ENABLE_DRYWALLS"));
        assertTrue(factory.contains("case ContentPolicy.ENABLE_ROCK_SALT_LAMPS"));
        assertTrue(factory.contains("case ContentPolicy.ENABLE_MINERAL_DUSTS"));
        assertTrue(factory.contains("case ContentPolicy.ENABLE_MINERAL_FERTILIZER"));
        assertTrue(factory.contains("Unknown Mineralogy recipe config flag"));

        assertTrue(generator.contains("ConfigCondition 'ENABLE_DRYWALLS'"));
        assertTrue(generator.contains("ConfigCondition 'ENABLE_ROCK_SALT_LAMPS'"));
        assertTrue(generator.contains("ConfigCondition 'ENABLE_MINERAL_DUSTS'"));
        assertTrue(generator.contains("ConfigCondition 'ENABLE_MINERAL_FERTILIZER'"));
        assertTrue(generator.contains("Synchronize-AdvancementConditions"));
        assertTrue(new File("src/main/resources/assets/mineralogy/recipes/_factories.json").isFile());
        assertTrue(new File("src/main/resources/assets/mineralogy/advancements/_factories.json").isFile());
        assertTrue(!new File("src/main/java/zone/moddev/mc/mineralogy/init/Recipes.java").exists());
    }

    @Test
    public void sourceWritesDefaultsOnlyForAConfigThatDidNotExist() throws Exception {
        String source = source("src/main/java/zone/moddev/mc/mineralogy/MineralogyConfig.java");
        int readPolicy = source.indexOf("contentPolicy = ContentPolicy.read(config)");
        int guardedSave = source.indexOf("if (!existing) {");

        assertTrue(readPolicy >= 0);
        assertTrue(guardedSave > readPolicy);
        assertEquals(1, countOccurrences(source, "config.save();"));
    }

    private static void assertPolicy(ContentPolicy policy, boolean drywalls,
            boolean lamps, boolean dusts, boolean fertilizer) {
        assertEquals(drywalls, policy.drywallsEnabled());
        assertEquals(lamps, policy.rockSaltLampsEnabled());
        assertEquals(dusts, policy.mineralDustsEnabled());
        assertEquals(fertilizer, policy.mineralFertilizerEnabled());
    }

    private static String source(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
    }

    private static int countOccurrences(String value, String search) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(search, offset)) >= 0) {
            count++;
            offset += search.length();
        }
        return count;
    }
}
