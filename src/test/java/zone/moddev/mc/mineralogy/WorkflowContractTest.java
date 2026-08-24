package zone.moddev.mc.mineralogy;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertTrue;

public class WorkflowContractTest {
    @Test
    public void ciAndTagStarterUseTheGuardedTargetContract() throws Exception {
        String ci = text(".github/workflows/ci.yml");
        assertTrue(ci.contains("name: Build, test, and audit"));
        assertTrue(ci.contains("master-1.13.2"));
        assertTrue(ci.contains("java-version: '8'"));
        assertTrue(ci.contains("verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums"));

        String build = text("build.gradle");
        assertTrue(build.contains("options.addBooleanOption('notimestamp', true)"));
        assertTrue(build.contains("task normalizeReobfuscatedJar(type: Zip, dependsOn: 'reobfJar')"));
        assertTrue(build.contains("preserveFileTimestamps = false"));
        assertTrue(build.contains("reproducibleFileOrder = true"));

        String tag = text(".github/workflows/release-on-tag.yml");
        assertTrue(tag.contains("'6.0.1.113021'"));
        assertTrue(tag.contains("Build, test, and audit"));
        assertTrue(tag.contains("--ref master-1.12"));
        assertTrue(tag.contains("deploy-release.yml"));
    }

    private static String text(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
    }
}
