package zone.moddev.mc.mineralogy.documentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class DocumentationContractTest {
    @Test
    public void guideInventoryAndBuildPackagingStayComplete() throws Exception {
        File[] files = new File("docs").listFiles((dir, name) -> name.endsWith(".md"));
        assertTrue(files != null);
        Set<String> actual = new HashSet<String>();
        for (File file : files) {
            actual.add(file.getName());
        }
        assertEquals(new HashSet<String>(Arrays.asList(
                "README.md", "PLAYER_GUIDE.md", "DEVELOPER_GUIDE.md",
                "CONTENT_CONFIG.md", "PROVIDER.md")), actual);

        String build = read(new File("build.gradle"));
        assertTrue(build.contains("into 'META-INF/mineralogy/docs'"));
    }

    @Test
    public void guidesDescribeTheTargetNative110Contract() throws Exception {
        String all = "";
        for (File file : new File("docs").listFiles((dir, name) -> name.endsWith(".md"))) {
            all += read(file) + "\n";
        }

        assertTrue(all.contains("Minecraft 1.10.2"));
        assertTrue(all.contains("Forge 12.18.3.2511"));
        assertTrue(all.contains("OreSpawn 4.0.6"));
        assertTrue(all.contains("schema 4"));
        assertTrue(all.contains("provider revision 3"));
        assertTrue(all.contains("assets/mineralogy/orespawn/provider.json"));
        assertTrue(all.contains("config/mineralogy-orespawn.json"));
        assertTrue(all.contains("GROUP_TABS_BY_TYPE"));
        assertTrue(all.contains("COBBLESTONE_EQUIVILENT"));

        assertFalse(all.contains("mineralogy-common.toml"));
        assertFalse(all.contains("data/mineralogy/orespawn/provider.json"));
        assertFalse(all.contains("Minecraft 1.18.2"));
        assertFalse(all.contains("Java 17"));
        assertFalse(all.toLowerCase().contains("deepslate"));
        assertFalse(all.contains("Y -48"));
    }

    private static String read(File file) throws Exception {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
