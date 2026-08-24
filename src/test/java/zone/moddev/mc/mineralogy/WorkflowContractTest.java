package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

public class WorkflowContractTest {
    @Test
    public void branchCiUsesTheAuditedBuildAndModernPinnedChecks() throws Exception {
        String ci = read(".github/workflows/ci.yml");
        String codeql = read(".github/workflows/codeql-analysis.yml");
        String wrapper = read(".github/workflows/validate-gradle-build.yml");

        assertTrue(ci.contains("master-1.10.2"));
        assertTrue(ci.contains("name: Build, test, and audit"));
        assertTrue(ci.contains("clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts"));
        assertTrue(ci.contains("verifyEclipseProductionClasspath"));
        assertTrue(ci.contains("CHANGELOG.txt"));
        assertTrue(codeql.contains("github/codeql-action/init@db488ddef3bf6cb639b32c2e9a7c0a7ea8271d28"));
        assertTrue(codeql.contains("./gradlew clean classes"));
        assertTrue(codeql.contains("--rerun-tasks --no-build-cache"));
        assertTrue(wrapper.contains("gradle/actions/wrapper-validation@9c971963bec38e04b3d30dcc455b5382be2fdbfb"));
    }

    @Test
    public void tagValidatorUsesVersionMetadataAndNeverDispatchesPublication() throws Exception {
        String tag = read(".github/workflows/release-on-tag.yml");
        assertTrue(tag.contains("'*.*.*.*'"));
        assertTrue(tag.contains("loader_name:$loader_code"));
        assertTrue(tag.contains("Build, test, and audit"));
        assertTrue(tag.contains("Release tag must equal mod_version"));
        assertTrue(tag.contains("confirm live publication: checked"));
        assertFalse(tag.contains("gh workflow run"));
        assertFalse(tag.contains("actions: write"));
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8)
                .replace("\r\n", "\n").replace('\r', '\n');
    }
}
