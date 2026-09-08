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
    public void releaseMetadataIdentifiesTheGenericForgeTarget() throws Exception {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("gradle.properties")) {
            properties.load(input);
        }
        assertEquals("6.1.2.2602001", properties.getProperty("mod_version"));
        assertEquals("26.2", properties.getProperty("minecraft_version"));
        assertEquals(properties.getProperty("mc_version"), properties.getProperty("minecraft_version"));
        assertEquals("forge", properties.getProperty("loader_name"));
        assertEquals("1", properties.getProperty("loader_code"));
        assertEquals("25", properties.getProperty("java_version"));
        assertEquals("25.0.3+9", properties.getProperty("java_toolchain_version"));
        assertEquals("25", properties.getProperty("gradle_java_version"));
        assertEquals("240974", properties.getProperty("curseforge_project_id"));
        assertEquals("zone.moddev.mc", properties.getProperty("mod_group"));
        assertEquals("4.0.16.2602001", properties.getProperty("orespawn_version"));
        assertEquals("8798746", properties.getProperty("orespawn_curse_file_id"));
        assertEquals("401C1351C4F5410CF3F31FCD2E1B6C7607F0676D8F584DBFAD7EDD611828ACC2",
                properties.getProperty("orespawn_sha256"));
        String wrapper = text("gradle/wrapper/gradle-wrapper.properties");
        assertTrue(wrapper.contains("gradle-9.6.1-bin.zip"));
        assertTrue(wrapper.contains("9c0f7faeeb306cb14e4279a3e084ca6b596894089a0638e68a07c945a32c9e14"));
    }

    @Test
    public void ciAndSecurityChecksUsePinnedTargetNativeBuilds() throws Exception {
        String ci = text(".github/workflows/ci.yml");
        String codeql = text(".github/workflows/codeql-analysis.yml");
        String wrapper = text(".github/workflows/validate-gradle-build.yml");
        String staging = text("gradle/stage-orespawn-release.sh");
        assertTrue(ci.contains("name: Build, test, and audit"));
        assertTrue(ci.contains("master-26.2"));
        assertTrue(ci.contains("Install pinned Java 25 runtime and toolchain"));
        assertTrue(ci.contains("java-version: '25.0.3+9.0.LTS'"));
        assertTrue(ci.contains("applied 0 rule(s) for net.minecraftforge:forge:26.2-65.1.0 (explicit no-op)"));
        assertTrue(ci.contains("-Dorg.gradle.java.installations.paths=\"$JAVA_HOME\""));
        assertFalse(ci.contains("JAVA_HOME_8_X64"));
        assertTrue(ci.contains("verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums"));
        assertTrue(ci.contains("genEclipseRuns isolateEclipseProductionRuns verifyEclipseProductionClasspath"));
        assertTrue(ci.contains("CHANGELOG.txt"));
        assertTrue(ci.contains("-PorespawnVerificationRepository=${{ steps.orespawn.outputs.repository }}"));
        assertTrue(staging.contains("https://www.curseforge.com/api/v1/mods/$project_id/files/$file_id/download"));
        assertTrue(staging.contains("sha256sum"));
        assertTrue(codeql.contains("github/codeql-action/init@db488ddef3bf6cb639b32c2e9a7c0a7ea8271d28"));
        assertTrue(codeql.contains("Install pinned Java 25 runtime and toolchain"));
        assertTrue(codeql.contains("-Dorg.gradle.java.installations.paths=$JAVA_HOME"));
        assertFalse(codeql.contains("JAVA_HOME_8_X64"));
        assertTrue(codeql.contains("clean classes --rerun-tasks --no-build-cache"));
        assertTrue(codeql.contains("--rerun-tasks --no-build-cache"));
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
        assertTrue(build.contains("filesMatching(['**/*.java', '**/*.json', '**/*.mcmeta', '**/*.toml'])"));
        assertTrue(build.contains("filesMatching(['**/*.json', '**/*.mcmeta', '**/*.toml', '**/*.md'])"));
        assertTrue(build.contains("ForgeGradle merged main output has multiple producers"));
        assertTrue(build.contains("'zone/moddev/mc/mineralogy/Mineralogy.class'"));
        assertTrue(build.contains("928 Forge 65 item definitions"));
        assertTrue(build.contains("assets/mineralogy/items/"));
        assertTrue(build.contains("def preparedReleaseDir = project.findProperty('preparedReleaseDir')"));
        assertTrue(build.contains("tasks.register('verifyPreparedReleaseArtifacts')"));
        assertTrue(build.contains("tasks.withType(PublishToMavenRepository).configureEach"));
        assertTrue(build.contains("tasks.register('verifyMavenCoordinates')"));
        assertTrue(build.contains("generatePomFileForMavenJavaPublication"));
        assertTrue(build.contains("dependsOn tasks.named('verifyMavenCoordinates')"));
        assertTrue(build.contains("expectedMavenGroup = 'zone.moddev.mc'"));
        assertTrue(build.contains("expectedMavenArtifact = 'Mineralogy'"));
        assertTrue(build.contains("'Maven-Artifact'"));
        assertTrue(build.contains("'Built-On-Java'         : '25'"));
        assertTrue(build.contains("args '--mixin', 'mineralogy.mixins.json'"));
        assertTrue(build.contains("Maven release publication must use a remote repository"));
        assertTrue(build.contains("name = 'release'"));
        assertFalse(build.contains("file:///${project.projectDir}/mcmodsrepo"));
    }

    @Test
    public void stagedOreSpawnDependencyCannotFallThroughToPublicRepositories() throws Exception {
        String build = text("build.gradle");
        assertTrue(build.contains("exclusiveContent"));
        assertTrue(build.contains("forRepository"));
        assertTrue(build.contains("includeModule('curse.maven', orespawnModule)"));
        assertTrue(build.contains("'OreSpawnReleaseVerificationMirror'"));
    }

    @Test
    public void eclipseLaunchesQuoteCompleteSlimeLauncherPaths() throws Exception {
        String build = text("build.gradle");
        assertTrue(build.contains("['cache', 'metadata'].each"));
        assertTrue(build.contains("['to-srg', 'to-obf'].each"));
        assertTrue(build.contains("ForgeGradle omitted required --${flag}"));
        assertTrue(build.contains("&quot;${value}&quot;"));
        assertTrue(build.contains("Eclipse launch does not quote its --${flag} path"));
        assertTrue(build.contains("if (launch.contains('-DlegacyClassPath.file=')"));
        assertTrue(build.contains("['src/main/resources', 'src/generated/resources'].contains(entry.path)"));
        assertTrue(build.contains("processedMainResourcesPath()"));
        assertTrue(build.contains("processedMainResourcesDirectory()"));
        assertTrue(build.contains("Eclipse must use processed production resources"));
        assertTrue(build.contains("defaultOutputDir = file('bin/default')"));
        assertTrue(build.contains("Eclipse outputs must be disjoint bin/default, bin/main and bin/test"));
        assertTrue(build.contains("exclude group: 'io.github.llamalad7', module: 'mixinextras-common'"));
        assertTrue(build.contains("configurations.compileClasspath.resolvedConfiguration"));
        assertTrue(build.contains("Eclipse production classpath contains duplicate MixinExtras common module"));
    }

    private static String text(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8)
                .replace("\r\n", "\n").replace('\r', '\n');
    }
}
