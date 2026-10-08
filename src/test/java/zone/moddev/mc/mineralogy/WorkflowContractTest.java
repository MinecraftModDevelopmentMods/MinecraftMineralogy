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

public class WorkflowContractTest {
    @Test
    public void releaseMetadataIdentifiesTheGenericNeoForgeTarget() throws Exception {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("gradle.properties")) {
            properties.load(input);
        }
        assertEquals("6.1.4.121012", properties.getProperty("mod_version"));
        assertEquals("1.21.1", properties.getProperty("minecraft_version"));
        assertEquals(properties.getProperty("mc_version"), properties.getProperty("minecraft_version"));
        assertEquals("neoforge", properties.getProperty("loader_name"));
        assertEquals("2", properties.getProperty("loader_code"));
        assertEquals("21", properties.getProperty("java_version"));
        assertEquals("21.0.7+6", properties.getProperty("java_toolchain_version"));
        assertEquals("21", properties.getProperty("gradle_java_version"));
        assertEquals("240974", properties.getProperty("curseforge_project_id"));
        assertEquals("zone.moddev.mc.mineralogy", properties.getProperty("mod_group"));
        assertEquals("4.0.16.121012", properties.getProperty("orespawn_version"));
        assertEquals("8807135", properties.getProperty("orespawn_curse_file_id"));
        assertEquals("B0659E071633D9EC42A96D9759895DE2F5126C205A4BDBFEE0EB8E3F226D158B",
                properties.getProperty("orespawn_sha256"));
    }

    @Test
    public void ciAndSecurityChecksUsePinnedTargetNativeBuilds() throws Exception {
        String ci = text(".github/workflows/ci.yml");
        String codeql = text(".github/workflows/codeql-analysis.yml");
        String wrapper = text(".github/workflows/validate-gradle-build.yml");
        String staging = text("gradle/stage-orespawn-release.sh");
        assertTrue(ci.contains("name: Build, test, and audit"));
        assertTrue(ci.contains("master-1.21.1-neo"));
        assertTrue(ci.contains("java-version: '21.0.7+6.0.LTS'"));
        assertTrue(ci.contains("Install exact Java 21"));
        assertTrue(ci.contains("Cold NeoForge bootstrap"));
        assertTrue(ci.contains("verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums"));
        assertTrue(ci.contains("eclipse verifyEclipseProductionClasspath"));
        assertTrue(ci.contains("CHANGELOG.txt"));
        assertTrue(ci.contains("PorespawnVerificationRepository"));
        assertFalse(ci.contains("genEclipseRuns"));
        assertFalse(ci.contains("Mavenizer"));
        assertFalse(ci.contains("JAVA_HOME_8_X64"));
        assertFalse(ci.contains("JAVA_HOME_25_X64"));
        assertTrue(staging.contains("https://www.curseforge.com/api/v1/mods/$project_id/files/$file_id/download"));
        assertTrue(staging.contains("sha256sum"));
        assertTrue(codeql.contains("github/codeql-action/init@db488ddef3bf6cb639b32c2e9a7c0a7ea8271d28"));
        assertTrue(codeql.contains("Install exact Java 21"));
        assertTrue(codeql.contains("clean classes --no-daemon --stacktrace --max-workers=2"));
        assertFalse(codeql.contains("Mavenizer"));
        assertTrue(wrapper.contains("gradle/actions/wrapper-validation@9c971963bec38e04b3d30dcc455b5382be2fdbfb"));
    }

    @Test
    public void tagValidatorIsGenericAndDoesNotDispatchPublication() throws Exception {
        String tag = text(".github/workflows/release-on-tag.yml");
        assertTrue(tag.contains("'*.*.*.*'"));
        assertTrue(tag.contains("loader_name:$loader_code"));
        assertTrue(tag.contains("Build, test, and audit"));
        assertTrue(tag.contains("Release tag must equal mod_version"));
        assertTrue(tag.contains("confirm live publication: checked"));
        assertFalse(tag.contains("gh workflow run"));
        assertFalse(tag.contains("actions: write"));
    }

    @Test
    public void buildPublishesOnlyThePreparedRemoteBundle() throws Exception {
        String build = text("build.gradle");
        assertTrue(build.contains("options.addBooleanOption('notimestamp', true)"));
        assertFalse(build.contains("net.minecraftforge.renamer"));
        assertTrue(build.contains("def releaseJar = tasks.named('jar', Jar)"));
        assertTrue(build.contains("preserveFileTimestamps = false"));
        assertTrue(build.contains("reproducibleFileOrder = true"));
        assertTrue(build.contains("def preparedReleaseDir = project.findProperty('preparedReleaseDir')"));
        assertTrue(build.contains("tasks.register('verifyPreparedReleaseArtifacts')"));
        assertTrue(build.contains("tasks.withType(PublishToMavenRepository).configureEach"));
        assertTrue(build.contains("tasks.register('verifyMavenCoordinates')"));
        assertTrue(build.contains("generatePomFileForMavenJavaPublication"));
        assertTrue(build.contains("dependsOn tasks.named('verifyMavenCoordinates')"));
        assertTrue(build.contains("expectedMavenGroup = 'zone.moddev.mc.mineralogy'"));
        assertTrue(build.contains("expectedMavenArtifact = 'Mineralogy'"));
        assertTrue(build.contains("'Maven-Artifact'"));
        assertTrue(build.contains("Maven release publication must use a remote repository"));
        assertTrue(build.contains("name = 'release'"));
        assertFalse(build.contains("file:///${project.projectDir}/mcmodsrepo"));
    }

    @Test
    public void oreSpawnUsesMmdMavenWithCurseAndSealedMirrorFallbacks() throws Exception {
        String build = text("build.gradle");
        assertTrue(build.contains("exclusiveContent"));
        assertTrue(build.contains("forRepositories"));
        assertTrue(build.contains("includeModule(orespawnMavenGroup, orespawnMavenArtifact)"));
        assertTrue(build.contains("?: 'https://maven.moddev.zone/releases'"));
        assertTrue(build.contains("?: 'https://www.cursemaven.com'"));
        assertTrue(build.contains("'CurseMavenOreSpawnFallback'"));
        assertTrue(build.contains("curse/maven/${orespawnCurseModule}"));
        assertTrue(build.contains("'OreSpawnReleaseVerificationMirror'"));
        assertTrue(build.contains("runtimeOnly(orespawnCoordinate) { transitive = false }"));
        assertFalse(build.contains("runtimeOnly \"curse.maven:mmd-orespawn-"));
    }

    @Test
    public void eclipseUsesNeoGradleProductionOutputs() throws Exception {
        String build = text("build.gradle");
        assertTrue(build.contains("['src/main/resources', 'src/generated/resources'].contains(entry.path)"));
        assertTrue(build.contains("'build/resources/main', 'bin/main'"));
        assertTrue(build.contains("synchronizationTasks 'prepareEclipseResources'"));
        assertTrue(build.contains("Eclipse must consume only Gradle-processed production resources"));
        assertTrue(build.contains("tasks.register('verifyEclipseProductionClasspath')"));
        assertFalse(build.contains("synchronizationTasks 'isolateEclipseProductionRuns'"));
        assertFalse(build.contains("genEclipseRuns"));
        assertFalse(build.contains("slime-launcher"));
    }

    @Test
    public void worldgenQualificationDelegatesToTheReleasedOreSpawnRuntime() throws Exception {
        String build = text("build.gradle");
        assertTrue(build.contains("configureOreSpawnQualification"));
        assertTrue(build.contains("orespawn.worldgenBenchmarkMode"));
        assertTrue(build.contains("orespawn.worldgenBenchmarkRadius"));
        assertTrue(build.contains("orespawn.worldgenBenchmarkDimension"));
        assertTrue(build.contains("orespawn.worldgenBenchmarkBiomeType"));
        assertFalse(new File("src/main/java/zone/moddev/mc/mineralogy/worldgen").exists());
    }

    private static String text(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8)
                .replace("\r\n", "\n").replace('\r', '\n');
    }
}
