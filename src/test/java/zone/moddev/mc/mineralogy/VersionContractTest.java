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
    public void minecraft1122ForgeUsesTheDeterministicFourComponentVersion() throws Exception {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("gradle.properties")) {
            properties.load(input);
        }

        String minecraftVersion = properties.getProperty("mc_version");
        String modVersion = properties.getProperty("mod_version");
        assertEquals("1.12.2", minecraftVersion);
        assertEquals("112021", targetFor(minecraftVersion, 1));
        assertEquals("6.0.1.112021", modVersion);
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
        assertTrue(build.contains("accessTransformer.from(file('gradle/orespawn-development-access-transformer.cfg'))"));
        assertTrue(build.contains("Development access transformer no longer matches released OreSpawn semantics"));
        assertTrue(build.contains("source sourceSets.main"));
        assertTrue(build.contains("tasks.register('prepareDevelopmentMod', Sync)"));
        assertTrue(build.contains("environment 'MOD_CLASSES', developmentModOutput.get().asFile.absolutePath"));
        assertFalse(build.contains("developmentLauncher"));
        assertTrue(build.contains("after ForgeGradle has finalized its run"));
        assertTrue(build.contains("tasks.register('verifyPreparedReleaseArtifacts')"));
        assertTrue(build.contains("inputs.files(providers.provider"));
        assertTrue(build.contains("tasks.register('writeReleaseChecksums')"));
        assertTrue(build.contains("providers.gradleProperty('preparedReleaseDir')"));
        assertTrue(wrapper.contains("gradle-9.6.1-bin.zip"));
        assertEquals("240974", properties.getProperty("cf_project_id"));
        assertEquals("mmd-orespawn", properties.getProperty("cf_requirements"));
        assertEquals("245586", properties.getProperty("orespawn_curse_project_id"));
        assertEquals("8685379", properties.getProperty("orespawn_curse_file_id"));
        assertEquals("4.0.7.112021", properties.getProperty("orespawn_version"));
        assertEquals("54508BEC9E08F2858CF1AEB0309EFCF64E89B035DF89CFD69D30BE182F981C80",
                properties.getProperty("orespawn_sha256"));
        assertFalse(properties.containsKey("create_api_jar"));
        assertFalse(properties.containsKey("create_deobf_jar"));
    }

    @Test
    public void productionMetadataDoesNotRetainTheLegacyFingerprintToken() throws Exception {
        String source = read(new File("src/main/java/zone/moddev/mc/mineralogy/Mineralogy.java"));
        assertFalse(source.contains("certificateFingerprint"));
        assertFalse(source.contains("@FINGERPRINT@"));
    }

    @Test
    public void eclipseSetupCreatesAndVerifiesTheRequiredBuildshipProjectConfiguration() throws Exception {
        String build = read(new File("build.gradle"));
        assertTrue(build.contains("tasks.register('configureEclipseBuildship')"));
        assertTrue(build.contains(".settings/org.eclipse.buildship.core.prefs"));
        assertTrue(build.contains("connection.gradle.distribution"));
        assertTrue(build.contains("GRADLE_DISTRIBUTION(WRAPPER)"));
        assertTrue(build.contains("connection.gradle.user.home"));
        assertTrue(build.contains("'override.workspace.settings'     : 'true'"));
        assertTrue(build.contains("finalizedBy configureEclipseBuildship"));
        assertTrue(build.contains("finalizedBy configureEclipseBuildship, 'isolateEclipseProductionRuns'"));
        assertTrue(build.contains("dependsOn tasks.named('genEclipseRuns')"));
        assertTrue(build.contains("Missing Eclipse Buildship project configuration"));
        assertTrue(build.contains("Eclipse Buildship Gradle home is not"));
        assertTrue(build.contains("splitProductionOutputs"));
        assertTrue(build.contains("setClasspath(project.files("));
        assertTrue(build.contains("developmentModOutput.get().asFile"));
        assertTrue(build.contains("['cache', 'metadata', 'to-srg', 'to-obf']"));
        assertTrue(build.contains("does not quote its --${flag} path"));
    }

    @Test
    public void humanGuideExplainsFunctionalAndTargetVersions() throws Exception {
        String guide = read(new File("docs/VERSIONS.md"));
        assertTrue(guide.contains("Major.Minor.Bug.Target"));
        assertTrue(guide.contains("6.0.1.112021"));
        assertTrue(guide.contains("1.12.2 | Forge | `112021`"));
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
