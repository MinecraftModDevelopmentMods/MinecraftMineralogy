package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.Test;

public class VersionContractTest {
    @Test
    public void minecraft1102ForgeUsesTheDeterministicFourComponentVersion() throws Exception {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("gradle.properties")) {
            properties.load(input);
        }

        String minecraftVersion = properties.getProperty("mc_version");
        String modVersion = properties.getProperty("mod_version");
        assertEquals("1.10.2", minecraftVersion);
        assertEquals("110021", targetFor(minecraftVersion, 1));
        assertEquals("6.0.1.110021", modVersion);
        assertEquals(modVersion, Mineralogy.VERSION);
        assertTrue(modVersion.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"));
    }

    @Test
    public void gradlePublishesTheQualifiedVersionWithoutCiSuffixes() throws Exception {
        String build = read(new File("build.gradle"));
        assertTrue(build.contains("version = project.mod_version"));
        assertTrue(build.contains("base.archivesName = 'Mineralogy'"));
        assertFalse(build.contains("Mineralogy-${project.mc_version}"));
        assertTrue(build.contains("artifact(releaseJar)"));
        assertTrue(build.contains("versionParts[3] != expectedTargetVersion"));
        assertTrue(build.contains("ext.release_tag = \"${project.mc_version}-${project.mod_version}\""));
        assertFalse(build.contains("BUILD_NUMBER"));
        assertFalse(build.contains("TRAVIS_BUILD_NUMBER"));
        assertFalse(build.contains("CIRCLE_BUILD_NUM"));
        assertFalse(build.contains("getModVersion"));
    }

    @Test
    public void modernBuildAndReleaseMetadataKeepTheRequiredOreSpawnContract() throws Exception {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("gradle.properties")) {
            properties.load(input);
        }

        String build = read(new File("build.gradle"));
        String wrapper = read(new File("gradle/wrapper/gradle-wrapper.properties"));
        assertTrue(build.contains("id 'net.minecraftforge.gradle' version '7.0.34'"));
        assertTrue(build.contains("id 'net.minecraftforge.renamer' version '1.1.5'"));
        assertTrue(build.contains("JavaLanguageVersion.of(8)"));
        assertTrue(build.contains("tasks.register('verifyReleaseDependencies')"));
        assertTrue(build.contains("tasks.register('verifyPreparedReleaseArtifacts')"));
        assertTrue(build.contains("providers.gradleProperty('preparedReleaseDir')"));
        assertTrue(wrapper.contains("gradle-9.6.1-bin.zip"));
        assertEquals("240974", properties.getProperty("cf_project_id"));
        assertEquals("mmd-orespawn", properties.getProperty("cf_requirements"));
        assertEquals("245586", properties.getProperty("orespawn_curse_project_id"));
        assertEquals("8681675", properties.getProperty("orespawn_curse_file_id"));
        assertEquals("4.0.6.110021", properties.getProperty("orespawn_version"));
        assertFalse(properties.containsKey("create_api_jar"));
        assertFalse(properties.containsKey("create_deobf_jar"));
    }

    @Test
    public void eclipseLaunchNormalizationQuotesWhitespacePathsAndVerifiesThem() throws Exception {
        String build = read(new File("build.gradle"));
        String project = read(new File(".project"));
        assertTrue(build.contains("tasks.register('configureEclipseBuildship')"));
        assertTrue(build.contains("tasks.register('isolateEclipseProductionRuns')"));
        assertTrue(build.contains("['--cache', '--metadata', '--to-srg', '--to-obf']"));
        assertTrue(build.contains("+ '&quot;' + value + '&quot;'"));
        assertTrue(build.contains("leaves whitespace unquoted"));
        assertTrue(build.contains("finalizedBy configureEclipseBuildship, 'isolateEclipseProductionRuns'"));
        assertTrue(project.contains("org.eclipse.buildship.core.gradleprojectnature"));
        assertTrue(project.contains("org.eclipse.buildship.core.gradleprojectbuilder"));
    }

    @Test
    public void productionMetadataDoesNotRetainTheLegacyFingerprintToken() throws Exception {
        String source = read(new File("src/main/java/zone/moddev/mc/mineralogy/Mineralogy.java"));
        assertTrue(source.contains("certificateFingerprint = \"\""));
        assertFalse(source.contains("@FINGERPRINT@"));
    }

    @Test
    public void humanGuideExplainsFunctionalAndTargetVersions() throws Exception {
        String guide = read(new File("docs/VERSIONS.md"));
        assertTrue(guide.contains("Major.Minor.Bug.Target"));
        assertTrue(guide.contains("6.0.1.110021"));
        assertTrue(guide.contains("1.10.2 | Forge | `110021`"));
        assertTrue(guide.contains("loader code `1` for Forge"));
        assertTrue(guide.contains("[6.0.0,7.0.0)"));
    }

    private static String targetFor(String minecraftVersion, int loaderCode) {
        String[] parts = minecraftVersion.split("\\.");
        String patch = parts.length == 3 ? parts[2] : "0";
        return parts[0] + padTwo(parts[1]) + padTwo(patch) + loaderCode;
    }

    private static String padTwo(String value) {
        return value.length() >= 2 ? value : "0" + value;
    }

    private static String read(File file) throws Exception {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
