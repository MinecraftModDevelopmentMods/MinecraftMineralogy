package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

public class WorkflowContractTest {
    @Test
    public void branchCiUsesTheGuardedBuildAndModernPinnedChecks() throws Exception {
        String ci = read(".github/workflows/ci.yml");
        String codeql = read(".github/workflows/codeql-analysis.yml");
        String wrapper = read(".github/workflows/validate-gradle-build.yml");

        assertTrue(ci.contains("master-1.12"));
        assertTrue(ci.contains("name: Build, test, and audit"));
        assertTrue(ci.contains("clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts"));
        assertTrue(ci.contains("verifyEclipseProductionClasspath"));
        assertTrue(ci.contains("CHANGELOG.txt"));
        assertTrue(codeql.contains("github/codeql-action/init@db488ddef3bf6cb639b32c2e9a7c0a7ea8271d28"));
        assertTrue(codeql.contains("./gradlew clean classes"));
        assertTrue(wrapper.contains("gradle/actions/wrapper-validation@9c971963bec38e04b3d30dcc455b5382be2fdbfb"));
    }

    @Test
    public void tagValidatorIsGenericReadOnlyAndNeverStartsASecondRelease() throws Exception {
        String starter = read(".github/workflows/release-on-tag.yml");
        assertTrue(starter.contains("'*.*.*.*'"));
        assertTrue(starter.contains("loader_name:$loader_code"));
        assertTrue(starter.contains("Build, test, and audit"));
        assertTrue(starter.contains("Release tag must equal mod_version"));
        assertTrue(starter.contains("confirm live publication: checked"));
        assertFalse(starter.contains("gh workflow run"));
        assertFalse(starter.contains("actions: write"));
    }

    @Test
    public void defaultDispatcherUsesVersionOnlyRoutingAndCurseForgeOnlyChannels() throws Exception {
        String deploy = read(".github/workflows/deploy-release.yml");
        assertTrue(deploy.contains("release_version:"));
        assertTrue(deploy.contains("curseforge_release_level:"));
        assertTrue(deploy.contains("confirm_live_publication:"));
        assertFalse(deploy.contains("      mode:"));
        assertFalse(deploy.contains("      release_ref:"));
        assertFalse(deploy.contains("      confirm_version:"));
        assertFalse(deploy.contains("environment:\n      name: release"));

        assertTrue(deploy.contains("target_suffix=\"${BASH_REMATCH[1]}\""));
        assertTrue(deploy.contains("loader_code=\"${target_suffix: -1}\""));
        assertTrue(deploy.contains("\"master-$mc_major.$mc_minor.$mc_patch$loader_suffix\""));
        assertTrue(deploy.contains("\"master-$mc_major.$mc_minor$loader_suffix\""));
        assertTrue(deploy.contains("forge:1) loader_display=Forge"));
        assertTrue(deploy.contains("neoforge:2) loader_display=NeoForge"));
        assertTrue(deploy.contains("Unexpected Mineralogy CurseForge project"));

        assertEquals("master-1.10.2", fullBranchFor("110021"));
        assertEquals("master-1.12.2", fullBranchFor("112021"));
        assertEquals("master-1.13.2", fullBranchFor("113021"));
    }

    @Test
    public void dispatcherProtectsPublishedVersionsAndUsesOneOrderedBundle() throws Exception {
        String deploy = read(".github/workflows/deploy-release.yml");
        assertTrue(deploy.contains("legacy_release_tag=\"$minecraft_version-$release_version\""));
        assertTrue(deploy.contains("was already published under historical tag"));
        assertTrue(deploy.contains("Existing tag $release_tag resolves to"));
        assertTrue(deploy.contains("already has a GitHub Release for exact tag"));
        assertTrue(deploy.contains("Create validated release tag"));
        assertTrue(deploy.contains("SHA256SUMS"));
        assertTrue(deploy.contains("curseforge-dependencies: 245586(required)"));
        assertTrue(deploy.contains("version-type: ${{ inputs.curseforge_release_level }}"));
        assertTrue(deploy.contains("github-prerelease: false"));
        assertTrue(deploy.contains("version-type: release"));

        int maven = deploy.indexOf("  publish_maven:");
        int curseForge = deploy.indexOf("  publish_curseforge:");
        int github = deploy.indexOf("  publish_github:");
        assertTrue(maven > 0 && curseForge > maven && github > curseForge);
        assertTrue(deploy.substring(curseForge, github).contains("- publish_maven"));
        assertTrue(deploy.substring(github).contains("- publish_curseforge"));
    }

    private static String fullBranchFor(String suffix) {
        String loaderCode = suffix.substring(suffix.length() - 1);
        if (!"1".equals(loaderCode) && !"2".equals(loaderCode)) {
            throw new IllegalArgumentException("Unknown loader code " + loaderCode);
        }
        String digits = suffix.substring(0, suffix.length() - 1);
        String patch = Integer.toString(Integer.parseInt(digits.substring(digits.length() - 2)));
        digits = digits.substring(0, digits.length() - 2);
        String minor = Integer.toString(Integer.parseInt(digits.substring(digits.length() - 2)));
        String major = digits.substring(0, digits.length() - 2);
        return "master-" + major + "." + minor + "." + patch + ("2".equals(loaderCode) ? "-neo" : "");
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8)
                .replace("\r\n", "\n").replace('\r', '\n');
    }
}
