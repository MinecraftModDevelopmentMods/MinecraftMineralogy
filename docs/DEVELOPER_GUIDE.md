# Mineralogy Developer Guide

## Responsibility Split

| Mineralogy owns | OreSpawn owns |
| --- | --- |
| Blocks, items, fluids, and tile entities | Terrain replacement and formations |
| Models, textures, language, recipes, and drops | Geomes and biome influences |
| Forge tag identities and old-save compatibility | Ore and fluid-deposit placement |
| Mineralogy's provider declaration and migration | Profiles, UI, retrogen, and templates |

Mineralogy requires OreSpawn `[4.0.6,5.0.0)`. Reusable worldgen integrations
belong in `zone.moddev.mc.orespawn.api` or an OreSpawn provider rather than
Mineralogy internals.

## Packaged Provider

The schema-4, revision-3 provider is at:

```text
src/main/resources/data/mineralogy/orespawn/provider.json
```

It declares 32 rock rules, sulfur, phosphorous, and nitrate ores, and the
provider-owned `mineralogy:fluid_deposit/crude_oil` rule. OreSpawn owns optional
vanilla-ore management and must not be duplicated in Mineralogy.

Keep provider-owned IDs stable. Existing worlds contain self-contained
profiles; provider updates must not overwrite established world choices. A pack
may supply an authoritative full override at
`config/mineralogy-orespawn.json`.

## Forge Tag Integration

All raw rocks use `forge:stone` and their material-specific Mineralogy tags.
When the historical `COBBLESTONE_EQUIVILENT` option is enabled, all 27 raw
families additionally use `cobblestone`; chert and pumice always retain that
identity. Gypsum, chalk, rock salt, and both rock salt lamps retain their
specialty aliases.

Use broad `stone` or `cobblestone` inputs only when any matching material is
valid. Recipes returning a Mineralogy construction form must use the exact
material and finish so basalt cannot produce a different rock's slab or wall.

## Crafting Data

All Mineralogy recipes are native Minecraft/Forge 1.13 JSON under
`data/mineralogy/recipes/`. Run `scripts/generate-recipes.ps1` after changing
the recipe matrix; it generates the 27 stone families and global recipes, then
retains the target-native smelting data, and ensures every recipe has a matching
unlock advancement with the same Forge
conditions. Unlocks use direct inventory ingredients instead of listening to
other recipe unlocks, which would recursively reveal an entire construction
tree. Polishing uses Minecraft 1.13's advancement requirements matrix to
require the matching source plus accepted sand; manually crafting a recipe is
the target-native fallback for Forge's delayed crafting-output inventory
trigger.

Reliefs preserve the historical two-stage contract. Nine exact matching
polished blocks produce 16 blank reliefs; the blank relief is then the input to
the marked relief recipes. Two matching left reliefs shapelessly produce two
right reliefs. Do not substitute unrelated tags for these exact items.
Do not reintroduce a parallel Java crafting registry.

## Backward Compatibility

Keep the `mineralogy` mod ID, every registry name, tile ID, NBT field, asset
path, recipe identity, patch alias, Forge tag identity, and provider rule
stable. Production Java packages use `zone.moddev.mc.mineralogy`; implementation
package names are not saved-world identities.

The legacy `GENERATE_*` flags can remove registrations on the next start. The
new issue-121 switches only change creative visibility and Mineralogy-owned
recipes, so existing content remains loadable.

Forge 25 converts pre-flattening chunks lazily. The legacy chunk-loader hook
must run before Mojang's data fixer, translate the historical rock-furnace tile
ID, and preserve already populated chunks from cross-boundary feature writes.
Its packaged runtime uses SRG method names, so transformer matching relies on
stable owners and descriptors and fails startup if an insertion point cannot be
found. Validate both previously unloaded occupied furnaces and new chunks at an
old-world boundary in the reobfuscated jar; a development launch alone cannot
prove this path.

## Building

The build uses the target-native Gradle 4.9 wrapper and ForgeGradle 3 while
compiling production and test bytecode with Java 8:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk1.8.0_202'
$env:GRADLE_USER_HOME='D:\MinecraftMineralogy\.gradle-verify-cache'
.\gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums --no-daemon
.\gradlew.bat genEclipseRuns verifyEclipseProductionClasspath --no-daemon
.\gradlew.bat assemble --no-daemon
```

Inspect complete client/server logs and test the reobfuscated jar with released
OreSpawn in a launcher-like Forge installation. The normal jar packages this
guide under `META-INF/mineralogy/docs/`.

The complete release version is `Major.Minor.Bug.Target`; see
[Mineralogy Versioning](VERSIONS.md). This branch validates target `113021`
for Minecraft 1.13.2 Forge and does not append CI build numbers.
