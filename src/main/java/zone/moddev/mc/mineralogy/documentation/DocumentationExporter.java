package zone.moddev.mc.mineralogy.documentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.loading.FMLPaths;

/** Copies the bundled human-facing Mineralogy guide beside its configuration. */
public final class DocumentationExporter {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final GuideFile[] FILES = {
            guide("README.md"), guide("PLAYER_GUIDE.md"), guide("DEVELOPER_GUIDE.md"),
            guide("CONTENT_CONFIG.md"), guide("PROVIDER.md"), guide("VERSIONS.md"),
            new GuideFile("examples/mineralogy-provider.json", "/data/mineralogy/orespawn/provider.json",
                    "src/main/resources/data/mineralogy/orespawn/provider.json")
    };

    private DocumentationExporter() {
    }

    public static void exportBundledGuide() {
        Path target = FMLPaths.CONFIGDIR.get().resolve("mineralogy-guide");
        try {
            int exported = exportMissing(target);
            if (exported > 0) LOGGER.info("Exported {} Mineralogy guide files to {}", exported, target.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.warn("Could not export the Mineralogy guide to {}", target.toAbsolutePath(), e);
        }
    }

    public static int exportMissing(Path targetRoot) throws IOException {
        int exported = 0;
        for (GuideFile file : FILES) {
            Path target = targetRoot.resolve(file.target);
            if (Files.exists(target)) continue;
            try (InputStream source = openSource(file)) {
                Files.createDirectories(target.getParent());
                Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
                try {
                    Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
                    moveIntoPlace(temporary, target);
                    exported++;
                } finally {
                    Files.deleteIfExists(temporary);
                }
            }
        }
        return exported;
    }

    private static InputStream openSource(GuideFile file) throws IOException {
        return openSource(file.resource, file.developmentSource);
    }

    static InputStream openSource(String resource, String developmentSource) throws IOException {
        InputStream bundled = DocumentationExporter.class.getResourceAsStream(resource);
        if (bundled != null) return bundled;

        // Eclipse rebuilds bin/main from source folders and removes the guide
        // files Gradle stages there. In an actual source checkout, recover from
        // the authoritative files without weakening packaged-jar validation.
        Path source = findDevelopmentSource(Paths.get("").toAbsolutePath(), developmentSource);
        if (source != null) {
            LOGGER.debug("Using source-checkout Mineralogy guide resource {}", source);
            return Files.newInputStream(source);
        }
        throw new IOException("Missing bundled documentation resource " + resource);
    }

    static Path findDevelopmentSource(Path start, String relative) {
        Path root = start.toAbsolutePath().normalize();
        for (int depth = 0; root != null && depth < 3; depth++, root = root.getParent()) {
            Path candidate = root.resolve(relative).normalize();
            if (Files.isRegularFile(root.resolve("build.gradle")) && Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static void moveIntoPlace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target);
        }
    }

    private static GuideFile guide(String name) {
        return new GuideFile(name, "/META-INF/mineralogy/docs/" + name, "docs/" + name);
    }

    private static final class GuideFile {
        private final String target;
        private final String resource;
        private final String developmentSource;
        private GuideFile(String target, String resource, String developmentSource) {
            this.target = target;
            this.resource = resource;
            this.developmentSource = developmentSource;
        }
    }
}
