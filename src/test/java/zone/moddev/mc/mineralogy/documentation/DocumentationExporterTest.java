package zone.moddev.mc.mineralogy.documentation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DocumentationExporterTest {
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void exportsAllSevenFilesAndTheExactProviderBytes() throws Exception {
        Path target = temporary.getRoot().toPath();

        assertEquals(7, DocumentationExporter.exportMissing(target));
        assertTrue(Files.isRegularFile(target.resolve("README.md")));
        assertTrue(Files.isRegularFile(target.resolve("PLAYER_GUIDE.md")));
        assertTrue(Files.isRegularFile(target.resolve("DEVELOPER_GUIDE.md")));
        assertTrue(Files.isRegularFile(target.resolve("CONTENT_CONFIG.md")));
        assertTrue(Files.isRegularFile(target.resolve("PROVIDER.md")));
        assertTrue(Files.isRegularFile(target.resolve("VERSIONS.md")));
        Path example = target.resolve("examples/mineralogy-provider.json");
        assertTrue(Files.isRegularFile(example));
        assertArrayEquals(Files.readAllBytes(Paths.get(
                "src/main/resources/assets/mineralogy/orespawn/provider.json")),
                Files.readAllBytes(example));
    }

    @Test
    public void repeatedExportsPreserveEditsAndRestoreOnlyMissingFiles() throws Exception {
        Path target = temporary.getRoot().toPath();
        assertEquals(7, DocumentationExporter.exportMissing(target));

        Path readme = target.resolve("README.md");
        byte[] localNote = "local pack note\n".getBytes(StandardCharsets.UTF_8);
        Files.write(readme, localNote);
        assertEquals(0, DocumentationExporter.exportMissing(target));
        assertArrayEquals(localNote, Files.readAllBytes(readme));

        Files.delete(target.resolve("PROVIDER.md"));
        assertEquals(1, DocumentationExporter.exportMissing(target));
        assertTrue(Files.isRegularFile(target.resolve("PROVIDER.md")));
        assertArrayEquals(localNote, Files.readAllBytes(readme));

        try (Stream<Path> files = Files.walk(target)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }
}
