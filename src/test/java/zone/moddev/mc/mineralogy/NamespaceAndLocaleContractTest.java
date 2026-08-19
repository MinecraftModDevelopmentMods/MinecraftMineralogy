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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NamespaceAndLocaleContractTest {
    @Test
    public void namespaceVersionAndOilIdentitiesAreStable() {
        assertEquals("zone.moddev.mc.mineralogy", Mineralogy.class.getPackage().getName());
        assertEquals("mineralogy", Mineralogy.MODID);
        assertEquals("6.0.0", Mineralogy.VERSION);
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
    public void allElevenLocalesHaveExactKeyParity() throws Exception {
        File directory = new File("src/main/resources/assets/mineralogy/lang");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".lang"));
        assertNotNull(files);
        assertEquals(11, files.length);
        Set<String> expected = keys(new File(directory, "en_US.lang"));
        for (File file : files) {
            assertEquals(file.getName(), expected, keys(file));
        }
        assertTrue(expected.contains("tile.mineralogy.crude_oil.name"));
        assertTrue(expected.contains("item.mineralogy.crude_oil_bucket.name"));
        assertTrue(expected.contains("fluid.mineralogy.crude_oil"));
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

    private static Set<String> keys(File file) throws Exception {
        Set<String> keys = new HashSet<String>();
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                assertTrue(file.getName() + " duplicate key", keys.add(trimmed.substring(0, trimmed.indexOf('='))));
            }
        }
        return keys;
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
