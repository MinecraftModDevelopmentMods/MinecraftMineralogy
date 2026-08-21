package zone.moddev.mc.mineralogy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import zone.moddev.mc.mineralogy.fluids.MineralogyFluids;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NamespaceAndLocaleContractTest {
    @Test
    public void namespaceVersionAndOilIdentitiesAreStable() {
        assertEquals("zone.moddev.mc.mineralogy", Mineralogy.class.getPackage().getName());
        assertEquals("mineralogy", Mineralogy.MODID);
        assertEquals("6.0.1.110021", Mineralogy.VERSION);
        assertEquals("mineralogy_crude_oil", MineralogyFluids.FLUID_NAME);
        assertEquals("crude_oil", MineralogyFluids.BLOCK_NAME);
        assertEquals("crude_oil_bucket", MineralogyFluids.BUCKET_NAME);
        assertNotEquals("crude_oil", MineralogyFluids.FLUID_NAME);
    }

    @Test
    public void metadataRequiresReleasedOreSpawnRange() throws Exception {
        JsonArray mods = new JsonParser().parse(new FileReader("src/main/resources/mcmod.info")).getAsJsonArray();
        JsonObject mod = mods.get(0).getAsJsonObject();
        assertEquals("mineralogy", mod.get("modid").getAsString());
        assertEquals("${version}", mod.get("version").getAsString());
        assertEquals("orespawn@[4.0.6,5.0.0)", mod.getAsJsonArray("dependencies").get(0).getAsString());
    }

    @Test
    public void allSeventeenLocalesHaveExactOrderedKeyParity() throws Exception {
        File directory = new File("src/main/resources/assets/mineralogy/lang");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".lang"));
        assertNotNull(files);
        Set<String> expectedFiles = new HashSet<String>(Arrays.asList(
                "de_AU.lang", "de_DE.lang",
                "en_CA.lang", "en_EN.lang", "en_GB.lang", "en_PT.lang", "en_US.lang",
                "es_ES.lang", "es_MX.lang",
                "fr_CA.lang", "fr_FR.lang",
                "ja_JP.lang", "ko_KR.lang",
                "pt_BR.lang", "pt_PT.lang",
                "ru_RU.lang", "zh_CN.lang"));
        Set<String> actualFiles = new HashSet<String>();
        for (File file : files) {
            actualFiles.add(file.getName());
        }
        assertEquals(expectedFiles, actualFiles);

        List<String> expected = keysInOrder(new File(directory, "en_US.lang"));
        assertEquals(936, expected.size());
        for (File file : files) {
            assertEquals(file.getName(), expected, keysInOrder(file));
        }

        assertLocalePair(directory, "de_AU.lang", "de_DE.lang");
        assertLocalePair(directory, "es_ES.lang", "es_MX.lang");
        assertLocalePair(directory, "fr_CA.lang", "fr_FR.lang");
        assertLocalePair(directory, "pt_BR.lang", "pt_PT.lang");

        assertTrue(expected.contains("tile.mineralogy.crude_oil.name"));
        assertTrue(expected.contains("item.mineralogy.crude_oil_bucket.name"));
        assertTrue(expected.contains("fluid.mineralogy.crude_oil"));
        assertTrue(expected.contains("itemGroup.mineralogy.rock"));
        assertTrue(expected.contains("itemGroup.mineralogy.stair"));
        assertTrue(expected.contains("itemGroup.mineralogy.slab"));
        assertTrue(expected.contains("itemGroup.mineralogy.wall"));
        assertTrue(expected.contains("itemGroup.mineralogy.item"));
    }

    @Test
    public void retiredProductionPackagesAndWorldgenAreAbsent() throws Exception {
        assertFalse(containsJava(new File("src/main/java/cyano/mineralogy")));
        assertFalse(containsJava(new File("src/main/java/zone/moddev/mc/mineralogy/worldgen")));
        List<String> main = Files.readAllLines(
                new File("src/main/java/zone/moddev/mc/mineralogy/Mineralogy.java").toPath(), StandardCharsets.UTF_8);
        String joined = String.join("\n", main);
        assertFalse(joined.contains("registerWorldGenerator"));
        assertFalse(joined.contains("StoneReplacer"));
        assertFalse(joined.contains("OreSpawner"));
        assertTrue("client fluid models must be registered during pre-init",
                joined.contains("MineralogyFluids.registerClientModels();"));
    }

    private static void assertLocalePair(File directory, String first, String second) throws Exception {
        assertArrayEquals(first + " and " + second + " must remain byte-identical",
                Files.readAllBytes(new File(directory, first).toPath()),
                Files.readAllBytes(new File(directory, second).toPath()));
    }

    private static List<String> keysInOrder(File file) throws Exception {
        List<String> ordered = new ArrayList<String>();
        Set<String> keys = new HashSet<String>();
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                String key = trimmed.substring(0, trimmed.indexOf('='));
                assertTrue(file.getName() + " duplicate key " + key, keys.add(key));
                ordered.add(key);
            }
        }
        return ordered;
    }

    private static boolean containsJava(File directory) {
        if (!directory.isDirectory()) {
            return false;
        }
        File[] children = directory.listFiles();
        if (children == null) {
            return false;
        }
        for (File child : children) {
            if ((child.isFile() && child.getName().endsWith(".java")) || containsJava(child)) {
                return true;
            }
        }
        return false;
    }
}
