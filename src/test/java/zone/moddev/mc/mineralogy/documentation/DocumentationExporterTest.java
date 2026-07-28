package zone.moddev.mc.mineralogy.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentationExporterTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void exportsCompleteGuideAndDoesNotOverwriteExistingFiles() throws Exception {
		assertEquals(7, DocumentationExporter.exportMissing(temporaryDirectory));
		assertTrue(Files.isRegularFile(temporaryDirectory.resolve("README.md")));
		assertTrue(Files.isRegularFile(temporaryDirectory.resolve("DEVELOPER_GUIDE.md")));
		assertTrue(Files.isRegularFile(temporaryDirectory.resolve(
				"examples/mineralogy-provider.json")));

		Path readme = temporaryDirectory.resolve("README.md");
		Files.write(readme, "local note".getBytes(StandardCharsets.UTF_8));
		assertEquals(0, DocumentationExporter.exportMissing(temporaryDirectory));
		assertEquals("local note", new String(Files.readAllBytes(readme),
				StandardCharsets.UTF_8));
	}
}
