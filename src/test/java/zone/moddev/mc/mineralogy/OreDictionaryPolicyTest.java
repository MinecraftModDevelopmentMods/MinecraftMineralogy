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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String source = mineralogySource();
        Matcher matcher = Pattern.compile("addStoneType\\(\\\"([^\\\"]+)\\\"").matcher(source);
        Set<String> registeredRocks = new LinkedHashSet<String>();
        while (matcher.find()) {
            registeredRocks.add(matcher.group(1));
        }

        assertEquals(RAW_ROCKS, registeredRocks);
        assertTrue(source.contains("OreDictionary.registerOre(\"stone\", rock);\n"
                + "        OreDictionary.registerOre(\"stone\" + oreDictName, rock);\n"
                + "        if (oreDictionaryPolicy.cobblestoneEquivalentEnabled()) {\n"
                + "            OreDictionary.registerOre(cobblestone, rock);\n"
                + "        }"));
    }

    @Test
    public void historicalCobblestoneAndSpecialtyAliasesRemainUnconditional() throws Exception {
        String source = mineralogySource();

        assertTrue(source.contains("OreDictionary.registerOre(cobblestone, blockChert);"));
        assertTrue(source.contains("OreDictionary.registerOre(cobblestone, blockPumice);"));
        assertTrue(source.contains("OreDictionary.registerOre(\"blockGypsum\", blockGypsum);"));
        assertTrue(source.contains("OreDictionary.registerOre(\"blockChalk\", blockChalk);"));
        assertTrue(source.contains("OreDictionary.registerOre(\"blockRocksalt\", blockSalt);"));
        assertTrue(source.contains("OreDictionary.registerOre(\"lampRocksalt\", blockRockSaltLamp);"));
        assertTrue(source.contains("OreDictionary.registerOre(\"lampRocksaltStreet\", blockRockSaltStreetLamp);"));
    }

    @Test
    public void newCompatibilityKeyKeepsTheExistingConfigNonRewriteGuard() throws Exception {
        String source = mineralogySource();
        int readPolicy = source.indexOf("oreDictionaryPolicy = OreDictionaryPolicy.read(config)");
        int guardedSave = source.indexOf("if (!configWasPresent) {\n            config.save();\n        }");

        assertTrue(readPolicy >= 0);
        assertTrue(guardedSave > readPolicy);
        assertEquals(1, countOccurrences(source, "config.save();"));
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
