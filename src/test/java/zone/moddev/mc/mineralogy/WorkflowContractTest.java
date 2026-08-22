package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

public class WorkflowContractTest {
    @Test
    public void branchCiUsesTheGuarded112BuildAndExactCheckName() throws Exception {
        String ci = read(".github/workflows/ci.yml");
        assertTrue(ci.contains("master-1.12"));
        assertTrue(ci.contains("name: Build, test, and audit"));
        assertTrue(ci.contains("clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts"));
        assertTrue(ci.contains("Mineralogy-6.0.1.112021.jar"));
        assertTrue(ci.contains("Mineralogy-6.0.1.112021-sources.jar"));
        assertTrue(ci.contains("Mineralogy-6.0.1.112021-javadoc.jar"));
    }

    @Test
    public void tagStarterAcceptsOnlyTheExact112TargetTagAfterGreenCi() throws Exception {
        String starter = read(".github/workflows/release-on-tag.yml");
        assertTrue(starter.contains("'1.12.2-*'"));
        assertTrue(starter.contains("\\.112021$"));
        assertTrue(starter.contains("Build, test, and audit"));
        assertTrue(starter.contains("--ref master-1.12"));
    }

    @Test
    public void defaultDispatcherRetains110MaintenanceAndAdds112() throws Exception {
        String deploy = read(".github/workflows/deploy-release.yml");
        assertTrue(deploy.contains("1.10.2) target_suffix=110021"));
        assertTrue(deploy.contains("1.12.2) target_suffix=112021"));
        assertTrue(deploy.contains("release_tag=\"$minecraft_version-$actual_version\""));
        assertTrue(deploy.contains("minecraft_version: ${{ steps.validate.outputs.minecraft_version }}"));
        assertTrue(deploy.contains("game-versions: ${{ needs.preflight.outputs.minecraft_version }}"));
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
    }
}
