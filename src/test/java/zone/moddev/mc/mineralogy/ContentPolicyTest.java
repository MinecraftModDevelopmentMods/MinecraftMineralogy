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
        String source = mineralogySource();

        assertTrue(source.contains("blockRockSaltLamp = registerBlock(new RockSaltLamp(), \"rocksaltlamp\")"));
        assertTrue(source.contains("blockRockSaltStreetLamp = registerBlock(new RockSaltStreetLamp(), \"rocksaltstreetlamp\", 16)"));
        assertTrue(source.contains("drywall[i] = registerBlock(new DryWall(colorSuffixes[i])"));
        assertTrue(source.contains("addOre(\"phosphorous_ore\", orePhosphorous, phosphorousPowder"));
        assertTrue(source.contains("OreDictionary.registerOre(\"dust\" + oreDictionaryName, item)"));
        assertTrue(source.contains("OreDictionary.registerOre(\"block\" + oreDictionaryName, block)"));
        assertTrue(source.contains("OreDictionary.registerOre(fertilizer, mineralFertilizer)"));
    }

    @Test
    public void sourceRoutesCreativeVisibilityThroughTheIndependentPolicies() throws Exception {
        String source = mineralogySource();

        assertTrue(source.contains("applyCreativeVisibility(blockRockSaltLamp, \"rocksaltlamp\", contentPolicy.rockSaltLampsEnabled())"));
        assertTrue(source.contains("applyCreativeVisibility(blockRockSaltStreetLamp, \"rocksaltstreetlamp\", contentPolicy.rockSaltLampsEnabled())"));
        assertTrue(source.contains("applyCreativeVisibility(drywall[i], \"drywall_\" + colorSuffixes[i], contentPolicy.drywallsEnabled())"));
        assertTrue(source.contains("addDust(\"sulfur_dust\", \"Sulfur\", contentPolicy.mineralDustsEnabled())"));
        assertTrue(source.contains("addBlock(\"sulfur_block\", \"Sulfur\", 0, contentPolicy.mineralDustsEnabled())"));
        assertTrue(source.contains("contentPolicy.mineralFertilizerEnabled() ? CreativeTabs.MATERIALS : null"));
        assertTrue(source.contains("item.setCreativeTab(null)"));
    }

    @Test
    public void sourceGatesEveryTargetedRecipePlan() throws Exception {
        String source = mineralogySource();

        assertEquals(2, countOccurrences(source, "if (contentPolicy.drywallsEnabled())"));
        assertEquals(2, countOccurrences(source, "if (contentPolicy.rockSaltLampsEnabled())"));
        assertEquals(1, countOccurrences(source, "if (contentPolicy.mineralDustsEnabled())"));
        assertEquals(1, countOccurrences(source, "if (contentPolicy.mineralFertilizerEnabled())"));
        assertEquals(4, countOccurrences(source, "new ItemStack(Items.GUNPOWDER, 4)"));
        assertTrue(source.contains("if (enabled) {\n            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(item, 9)"));
        assertTrue(source.contains("if (enabled) {\n            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(block)"));
    }

    @Test
    public void sourceWritesDefaultsOnlyForAConfigThatDidNotExist() throws Exception {
        String source = mineralogySource();
        int readPolicy = source.indexOf("contentPolicy = ContentPolicy.read(config)");
        int guardedSave = source.indexOf("if (!configWasPresent) {\n            config.save();\n        }");

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

    private static String mineralogySource() throws Exception {
        return new String(Files.readAllBytes(new File(
                "src/main/java/zone/moddev/mc/mineralogy/Mineralogy.java").toPath()),
                StandardCharsets.UTF_8);
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
