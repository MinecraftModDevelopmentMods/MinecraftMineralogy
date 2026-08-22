# Mineralogy Developer Guide

## Responsibility Split

| Mineralogy owns | OreSpawn owns |
| --- | --- |
| Blocks, items, fluids, and tile entities | Terrain replacement and formations |
| Models, textures, language, recipes, and drops | Geomes and biome influences |
| Ore Dictionary identities and old-save compatibility | Ore and fluid-deposit placement |
| Mineralogy's provider declaration and migration | Profiles, UI, retrogen, and templates |

Mineralogy requires OreSpawn `[4.0.7,5.0.0)`. Reusable worldgen integrations
belong in `zone.moddev.mc.orespawn.api` or an OreSpawn provider rather than
Mineralogy internals.

## Packaged Provider

The schema-4, revision-3 provider is at:

```text
src/main/resources/assets/mineralogy/orespawn/provider.json
```

It declares 32 rock rules, sulfur, phosphorous, and nitrate ores, and the
provider-owned `mineralogy:fluid_deposit/crude_oil` rule. OreSpawn owns optional
vanilla-ore management and must not be duplicated in Mineralogy.

Keep provider-owned IDs stable. Existing worlds contain self-contained
profiles; provider updates must not overwrite established world choices. A pack
may supply an authoritative full override at
`config/mineralogy-orespawn.json`.

## Ore Dictionary Integration

All raw rocks use `stone` and their material-specific `stone<Name>` identity.
When the historical `COBBLESTONE_EQUIVILENT` option is enabled, all 27 raw
families additionally use `cobblestone`; chert and pumice always retain that
identity. Gypsum, chalk, rock salt, and both rock salt lamps retain their
specialty aliases.

Use broad `stone` or `cobblestone` inputs only when any matching material is
valid. Recipes returning a Mineralogy construction form must use the exact
material and finish so basalt cannot produce a different rock's slab or wall.

## Backward Compatibility

Keep the `mineralogy` mod ID, every registry name, tile ID, NBT field, asset
path, recipe identity, patch alias, Ore Dictionary identity, and provider rule
stable. Production Java packages use `zone.moddev.mc.mineralogy`; implementation
package names are not saved-world identities.

The legacy `GENERATE_*` flags can remove registrations on the next start. The
new issue-121 switches only change creative visibility and Mineralogy-owned
recipes, so existing content remains loadable.

## Building

The build uses Gradle 9.6.1 and ForgeGradle 7.0.34 while compiling all
production and test bytecode with a Java 8 toolchain. Use a Java 17-or-newer
runtime for Gradle and make JDK 8 available to toolchain discovery:

```powershell
$env:JAVA_HOME='<path to a Java 17-or-newer runtime>'
$env:GRADLE_USER_HOME='D:\MinecraftMineralogy\.gradle-verify-cache'
.\gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums --no-daemon
.\gradlew.bat genEclipseRuns verifyEclipseProductionClasspath --no-daemon
.\gradlew.bat assemble --no-daemon
```

Inspect complete client/server logs and test the reobfuscated jar with released
OreSpawn in a launcher-like Forge installation. The normal jar packages this
guide under `META-INF/mineralogy/docs/`.

The complete release version is `Major.Minor.Bug.Target`; see
[Mineralogy Versioning](VERSIONS.md). This branch validates target `112021`
for Minecraft 1.12.2 Forge and does not append CI build numbers.
