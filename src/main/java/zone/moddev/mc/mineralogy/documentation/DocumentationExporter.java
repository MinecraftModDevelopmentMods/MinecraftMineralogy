package zone.moddev.mc.mineralogy.documentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Loader;

/** Copies Mineralogy's bundled public guide beside its configuration on first use. */
public final class DocumentationExporter {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final GuideFile[] FILES = {
            guide("README.md"),
            guide("PLAYER_GUIDE.md"),
            guide("DEVELOPER_GUIDE.md"),
            guide("CONTENT_CONFIG.md"),
            guide("PROVIDER.md"),
            new GuideFile("examples/mineralogy-provider.json",
                    "/assets/mineralogy/orespawn/provider.json")
    };

    private DocumentationExporter() {
    }

    public static void exportBundledGuide() {
        Path target = Loader.instance().getConfigDir().toPath().resolve("mineralogy-guide");
        try {
            int exported = exportMissing(target);
            if (exported > 0) {
                LOGGER.info("Exported {} Mineralogy guide files to {}", exported,
                        target.toAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.warn("Could not export the Mineralogy guide to {}",
                    target.toAbsolutePath(), e);
        }
    }

    /** Writes missing files only, so pack authors may annotate their exported copy. */
    public static int exportMissing(Path targetRoot) throws IOException {
        int exported = 0;
        for (GuideFile file : FILES) {
            Path target = targetRoot.resolve(file.target);
            if (Files.exists(target)) {
                continue;
            }
            try (InputStream source = DocumentationExporter.class.getResourceAsStream(file.resource)) {
                if (source == null) {
                    throw new IOException("Missing bundled documentation resource " + file.resource);
                }
                Files.createDirectories(target.getParent());
                Path temporary = Files.createTempFile(target.getParent(),
                        target.getFileName().toString(), ".tmp");
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

    private static void moveIntoPlace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target);
        }
    }

    private static GuideFile guide(String name) {
        return new GuideFile(name, "/META-INF/mineralogy/docs/" + name);
    }

    private static final class GuideFile {
        private final String target;
        private final String resource;

        private GuideFile(String target, String resource) {
            this.target = target;
            this.resource = resource;
        }
    }
}
