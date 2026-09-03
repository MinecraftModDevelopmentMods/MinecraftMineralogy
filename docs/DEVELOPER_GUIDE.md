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

All raw rocks use their material-specific Mineralogy tags and the appropriate
common stone identities.
When the historical `COBBLESTONE_EQUIVILENT` option is enabled, all 27 raw
families additionally use `cobblestone`; chert and pumice always retain that
identity. Gypsum, chalk, rock salt, and both rock salt lamps retain their
specialty aliases.

Minecraft 1.21.1's `minecraft:stone_crafting_materials` and
`minecraft:stone_tool_materials` item tags include `#c:cobblestones`, so
enabled Mineralogy rocks work in native tool recipes. Forge 52 exposes
immutable tag snapshots; Mineralogy rebuilds only the block and item tag
membership after initial tag loading and every data reload, preserves other
mods' members, updates the exact tag instances retained by parsed recipes,
then invalidates recipe ingredient caches. Canonical `c:` tags and retained
legacy `forge:` aliases are updated together, as are the nested Minecraft
crafting and tool tags.

Sixteen established vanilla recipes and their advancements use conditional JSON overrides
for the complete exact-cobblestone, stone-crafting-material, and
stone-tool-material contracts. Enabled branches use stable Mineralogy union
tags; disabled branches restore the target-native ingredients. Forge 52's
already-resolved nested tags do not observe a replacement tag collection, so
Mineralogy also mutates retained tag instances rather than swapping the
collection.

Minecraft 1.21.1 retains three additional configurable recipes: coast, sentry, and vex
armor-trim template duplication. Enabled branches use the Mineralogy
cobblestone union; disabled branches preserve the recipes' exact vanilla
cobblestone ingredient. Their vanilla advancements are intentionally untouched
because those recipes unlock from owning the template, not from cobblestone.

Minecraft 1.21.1 also owns andesite, basalt, diorite, granite, tuff, and several
matching finishes. Mineralogy's family tags include both native and retained
legacy identities. Five `data/minecraft/recipe/polished_*.json` overrides move
the native polished-block route from 2x2 crafting to one exact native block plus
sand. That leaves 2x2 matching raw blocks available for Mineralogy bricks. The
matching vanilla advancements are overridden too, so native polishing is
revealed only after the player has both the exact native rock and sand.

Native tuff now owns raw, polished, brick, stair, slab, wall, and chiseled
forms. Those items join Mineralogy's tuff-family tags only where no competing
vanilla output exists. Vanilla construction and chiseled recipes remain
authoritative, including polished tuff to tuff bricks. The five polished-block
overrides make tuff plus sand produce polished tuff while the 2x2 raw route
remains available for Mineralogy tuff bricks. `minecraft:smooth_basalt` joins
the smooth-basalt family and can unlock and craft Mineralogy smooth-basalt
forms, while Minecraft's basalt-to-smooth-basalt smelting recipe remains
intact. Deepslate is deliberately only an OreSpawn terrain and ore host, not a
Mineralogy construction family.

Do not broaden vanilla slab, stair, or wall recipes. Where Minecraft already
owns a matching form (raw and polished andesite/diorite/granite slabs and
stairs, plus their raw walls), the Mineralogy-output recipe keeps an exact
legacy Mineralogy input except for the deliberate slab overrides below. Basalt
has no native construction forms, so its safe family tags feed Mineralogy
slabs, stairs, and walls. Tuff has complete native construction forms, so the
Mineralogy construction recipes use exact inputs wherever a broad family tag
would compete. Polished andesite/diorite/granite have no native walls, so those
three Mineralogy wall recipes may also accept the matching family tag.

The nine shared raw/polished/brick slab crafting recipes and fifteen
corresponding stonecutting routes deliberately output Mineralogy's
upright-capable slabs. Eighteen exact shapeless recipes provide reversible 1:1
conversion between those Mineralogy slabs and their retained vanilla
counterparts. The six new tuff bridges preserve access to native chiseled-tuff
recipes as well as exact-item modpack compatibility.

Use broad `stone` or `cobblestone` inputs only when any matching material is
valid. Recipes returning a Mineralogy construction form must use the exact
material and finish so basalt cannot produce a different rock's slab or wall.

## Crafting Data

All Mineralogy recipes are native Minecraft/Forge 1.21.1 JSON under
`data/mineralogy/recipe/`. Minecraft 1.21 uses singular registry data
directories (`recipe`, `advancement`, `loot_table`, and `tags/item|block|fluid`).
Run `scripts/generate-recipes.ps1` after changing
the recipe matrix; it generates the 27 stone families and global recipes, the
native slab/stonecutting overrides and compatibility conversions, and the five
target-native polished-block recipe/advancement overrides, then retains the
target-native smelting data. Every Mineralogy recipe has a matching unlock
advancement with the same Forge conditions and the same exact-item or
family-tag material predicate as the recipe. Unlocks use direct inventory
ingredients instead of listening to other recipe unlocks, which would
recursively reveal an entire construction tree. Polishing uses Minecraft
1.21.1's advancement requirements matrix to require the matching source plus
accepted sand; manually crafting a recipe is the target-native fallback for
Forge's delayed crafting-output inventory trigger. Rock-furnace advancements
use the matching slab-family tag as their sole material criterion. They
deliberately do not require an already-owned vanilla furnace, so the upgrade
route is visible before that intermediate is crafted.
Every generated recipe advancement, including each conditional Minecraft
payload, explicitly sets `sends_telemetry_event` to `false`.

Reliefs preserve the historical two-stage contract. Nine matching polished
blocks produce 16 blank reliefs; a target-native synonym may satisfy the exact
rock-family tag. The blank relief is then the exact input to the marked relief
recipes. Two matching left reliefs shapelessly produce two right reliefs. Do
not substitute unrelated materials or broad stone tags for these inputs.
Do not reintroduce a parallel Java crafting registry.

## Backward Compatibility

Keep the `mineralogy` mod ID, every registry name, tile ID, NBT field, asset
path, recipe identity, patch alias, Forge tag identity, and provider rule
stable. Production Java packages use `zone.moddev.mc.mineralogy`; implementation
package names are not saved-world identities.

The legacy `GENERATE_*` flags can remove registrations on the next start. The
new issue-121 switches only change creative visibility and Mineralogy-owned
recipes, so existing content remains loadable.

Forge 52 converts pre-flattening chunks lazily. The coremod expands Minecraft's
fixed legacy state tables before conversion, and the selected-world hook
installs the complete saved block mapping before Mojang's data fixer. It
reinstalls that mapping after the client enumerates other old saves, normalizes
legacy rock-furnace tile IDs, retains sidecar recovery, and protects populated
chunks from cross-boundary feature writes. Its packaged runtime uses SRG method
names, so transformer matching relies on stable owners and descriptors and
fails startup if an insertion point cannot be found. Validate both previously
unloaded occupied furnaces and new chunks at an old-world boundary in the
reobfuscated jar; a development launch alone cannot prove this path.

## Building

The build uses ForgeGradle 7.0.34 and the Gradle 9.6.1 wrapper on Java 21,
while an exact Java 21 toolchain compiles production and test bytecode:

```powershell
$env:JAVA_HOME='path-to-a-Java-21-jdk'
$env:GRADLE_USER_HOME='D:\MinecraftMineralogy\.gradle-verify-cache'
.\gradlew.bat clean check build javadoc verifyReleaseConfiguration verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums --no-daemon
.\gradlew.bat genEclipseRuns eclipse isolateEclipseProductionRuns verifyEclipseProductionClasspath --no-daemon
.\gradlew.bat assemble --no-daemon
```

Inspect complete client/server logs and test the reobfuscated jar with released
OreSpawn in a launcher-like Forge installation. The normal jar packages this
guide under `META-INF/mineralogy/docs/`.

The complete release version is `Major.Minor.Bug.Target`; see
[Mineralogy Versioning](VERSIONS.md). This branch validates target `121011`
for Minecraft 1.21.1 Forge and does not append CI build numbers.
