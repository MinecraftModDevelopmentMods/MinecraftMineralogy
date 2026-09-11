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
        assertEquals("6.1.2.2602002", properties.getProperty("mod_version"));
        assertEquals("26.2", properties.getProperty("minecraft_version"));
        assertEquals(properties.getProperty("mc_version"), properties.getProperty("minecraft_version"));
        assertEquals("neoforge", properties.getProperty("loader_name"));
        assertEquals("2", properties.getProperty("loader_code"));
        assertEquals("25", properties.getProperty("java_version"));
        assertEquals("25.0.3+9", properties.getProperty("java_toolchain_version"));
        assertEquals("25", properties.getProperty("gradle_java_version"));
        assertEquals("240974", properties.getProperty("curseforge_project_id"));
        assertEquals("zone.moddev.mc.mineralogy", properties.getProperty("mod_group"));
        assertEquals("4.0.16.2602002", properties.getProperty("orespawn_version"));
        assertEquals("8830635", properties.getProperty("orespawn_curse_file_id"));
        assertEquals("16DFD59B5083BB1F7FE399091AC9778F9D10F397FE214865C937F00D1C20625A",
                properties.getProperty("orespawn_sha256"));
        assertFalse(properties.containsKey("neogradle.subsystems.decompiler.enabled"));
        String wrapper = text("gradle/wrapper/gradle-wrapper.properties");
        assertTrue(wrapper.contains("gradle-9.2.1-bin.zip"));
    }

    @Test
    public void ciAndSecurityChecksUsePinnedTargetNativeBuilds() throws Exception {
        String ci = text(".github/workflows/ci.yml");
        String codeql = text(".github/workflows/codeql-analysis.yml");
        String wrapper = text(".github/workflows/validate-gradle-build.yml");
        String staging = text("gradle/stage-orespawn-release.sh");
        assertTrue(ci.contains("name: Build, test, and audit"));
        assertTrue(ci.contains("master-26.2-neo"));
        assertTrue(ci.contains("java-version: '25.0.3+9.0.LTS'"));
        assertTrue(ci.contains("Install exact Java 25"));
        assertTrue(ci.contains("Cold NeoForge bootstrap"));
        assertFalse(ci.contains("./gradlew clean check"));
        assertTrue(ci.contains("./gradlew clean \\"));
        assertTrue(ci.contains("./gradlew check build javadoc"));
        assertTrue(ci.contains("verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums"));
        assertTrue(ci.contains("eclipse verifyEclipseProductionClasspath"));
        assertTrue(ci.contains("CHANGELOG.txt"));
        assertTrue(ci.contains("PorespawnVerificationRepository"));
        assertFalse(ci.contains("genEclipseRuns"));
        assertFalse(ci.contains("Mavenizer"));
        assertFalse(ci.contains("neogradle.subsystems.decompiler.enabled=true"));
        assertFalse(ci.contains("JAVA_HOME_8_X64"));
        assertFalse(ci.contains("JAVA_HOME_25_X64"));
        assertTrue(staging.contains("https://www.curseforge.com/api/v1/mods/$project_id/files/$file_id/download"));
        assertTrue(staging.contains("sha256sum"));
        assertTrue(codeql.contains("github/codeql-action/init@db488ddef3bf6cb639b32c2e9a7c0a7ea8271d28"));
        assertTrue(codeql.contains("Install exact Java 25"));
        assertFalse(codeql.contains("clean classes"));
        assertTrue(codeql.contains("./gradlew clean --no-daemon --stacktrace --max-workers=2"));
        assertTrue(codeql.contains("./gradlew classes --no-daemon --stacktrace --max-workers=2"));
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
        assertTrue(build.contains("filesMatching('META-INF/neoforge.mods.toml')"));
        assertTrue(build.contains("from('docs')"));
        assertTrue(build.contains("NeoForge 26.2 uses official names"));
        assertTrue(build.contains("'zone/moddev/mc/mineralogy/Mineralogy.class'"));
        assertTrue(build.contains("928 NeoForge 26.2 item definitions"));
        assertTrue(build.contains("task.name.startsWith('cacheVersionExecutable')"));
        assertTrue(build.contains("NeoForge 26.2 must retain its validated binary userdev path"));
        assertTrue(build.contains("assets/mineralogy/items/"));
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
