package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import net.minecraftforge.common.config.Configuration;

public class OreDictionaryPolicyTest {
    private static final Set<String> RAW_ROCKS = new LinkedHashSet<String>(Arrays.asList(
            "diabase", "gabbro", "peridotite", "basaltic_glass", "scoria", "tuff",
            "andesite", "basalt", "diorite", "granite", "rhyolite", "pegmatite",
            "siltstone", "shale", "conglomerate", "dolomite", "limestone",
            "hornfels", "quartzite", "novaculite", "slate", "schist", "gneiss",
            "marble", "phyllite", "amphibolite", "rock_salt"));

    @Test
    public void cobblestoneEquivalenceDefaultsToEnabled() {
        assertTrue(OreDictionaryPolicy.defaults().cobblestoneEquivalentEnabled());
    }

    @Test
    public void historicalConfigKeyCanDisableCobblestoneEquivalenceWithoutSaving() {
        Configuration config = mock(Configuration.class);
        when(config.getBoolean(eq(OreDictionaryPolicy.COBBLESTONE_EQUIVILENT),
                eq(OreDictionaryPolicy.CATEGORY), eq(true), anyString())).thenReturn(false);

        assertFalse(OreDictionaryPolicy.read(config).cobblestoneEquivalentEnabled());
        verify(config).getBoolean(eq("COBBLESTONE_EQUIVILENT"), eq("options"),
                eq(true), anyString());
        verify(config, never()).save();
    }

    @Test
    public void everyRawRockUsesTheConditionalCobblestoneRegistration() throws Exception {
        Set<String> registeredRocks = zone.moddev.mc.mineralogy.data.MaterialData.toArray().stream()
                .map(material -> material.materialName.toLowerCase())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        registeredRocks.add("rock_salt");
        String source = eventSubscriberSource();

        assertEquals(RAW_ROCKS, registeredRocks);
        assertTrue(source.contains("for (Material material : MaterialData.toArray())"));
        assertTrue(source.contains("registerRawStone(material.materialName.toLowerCase())"));
        assertTrue(source.contains("registerRawStone(Constants.ROCKSALT.toLowerCase())"));
        assertTrue(source.contains("if (MineralogyConfig.makeRockCobblestoneEquivilent())"));
        assertTrue(source.contains("registerRawCobblestone(material.materialName.toLowerCase())"));
        assertTrue(source.contains("registerRawCobblestone(Constants.ROCKSALT.toLowerCase())"));
    }

    @Test
    public void historicalCobblestoneAndSpecialtyAliasesRemainUnconditional() throws Exception {
        String source = eventSubscriberSource();
        String blocks = source("src/main/java/zone/moddev/mc/mineralogy/init/Blocks.java");

        assertTrue(source.contains("registerRawCobblestone(Constants.CHERT)"));
        assertTrue(source.contains("registerRawCobblestone(Constants.PUMICE)"));
        assertTrue(blocks.contains("Constants.BLOCK_GYPSUM"));
        assertTrue(blocks.contains("Constants.BLOCK_CHALK"));
        assertTrue(blocks.contains("Constants.BLOCK_ROCKSALT"));
        assertTrue(blocks.contains("\"lampRocksalt\""));
        assertTrue(blocks.contains("\"lampRocksaltStreet\""));
        assertTrue(source.contains("OreDictionary.registerOre(map.getKey(), map.getValue())"));
    }

    @Test
    public void newCompatibilityKeyKeepsTheExistingConfigNonRewriteGuard() throws Exception {
        String source = source("src/main/java/zone/moddev/mc/mineralogy/MineralogyConfig.java");
        int readPolicy = source.indexOf("oreDictionaryPolicy = OreDictionaryPolicy.read(config)");
        int guardedSave = source.indexOf("if (!existing) {");

        assertTrue(readPolicy >= 0);
        assertTrue(guardedSave > readPolicy);
        assertEquals(1, countOccurrences(source, "config.save();"));
    }

    private static String eventSubscriberSource() throws Exception {
        return source("src/main/java/zone/moddev/mc/mineralogy/MineralogyEventBusSubscriber.java");
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
