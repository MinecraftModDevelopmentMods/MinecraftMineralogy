package zone.moddev.mc.mineralogy.documentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;

import org.junit.Test;

public class DocumentationExporterSourceTest {
    @Test
    public void eclipseRunDirectoryFindsAuthoritativeGuideSources() {
        Path project = Paths.get("").toAbsolutePath().normalize();
        Path expected = project.resolve("docs/README.md").normalize();
        assertEquals(expected,
                DocumentationExporter.findDevelopmentSource(project.resolve("run"), "docs/README.md"));
    }

    @Test
    public void unrelatedDirectoryDoesNotBecomeADevelopmentFallback() throws Exception {
        Path unrelated = Files.createTempDirectory("mineralogy-guide-unrelated");
        assertNull(DocumentationExporter.findDevelopmentSource(unrelated, "docs/README.md"));
    }

    @Test
    public void missingEclipseClasspathResourceReadsAuthoritativeSourceFile() throws Exception {
        byte[] expected = Files.readAllBytes(Paths.get("docs/README.md"));
        byte[] actual = new byte[expected.length];
        try (InputStream source = DocumentationExporter.openSource(
                "/missing-from-eclipse-bin-main.md", "docs/README.md")) {
            int offset = 0;
            while (offset < actual.length) {
                int read = source.read(actual, offset, actual.length - offset);
                if (read < 0) break;
                offset += read;
            }
            assertEquals(expected.length, offset);
        }
        org.junit.Assert.assertArrayEquals(expected, actual);
    }
}
