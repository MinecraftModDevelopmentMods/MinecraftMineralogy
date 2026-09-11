# Mineralogy Developer Guide

## Responsibility Split

| Mineralogy owns | OreSpawn owns |
| --- | --- |
| Blocks, items, fluids, and tile entities | Terrain replacement and formations |
| Models, textures, language, recipes, and drops | Geomes and biome influences |
| Common and compatibility tag identities and old-save compatibility | Ore and fluid-deposit placement |
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

## NeoForge Tag Integration

Mineralogy uses the `c:` namespace for canonical interoperability tags and
retains narrowly scoped `forge:` aliases for older consumers. All raw rocks use
their material-specific Mineralogy tags.
When the historical `COBBLESTONE_EQUIVILENT` option is enabled, all 27 raw
families additionally use `cobblestone`; chert and pumice always retain that
identity. Gypsum, chalk, rock salt, and both rock salt lamps retain their
specialty aliases.

Minecraft 26.2's `minecraft:stone_crafting_materials` and
`minecraft:stone_tool_materials` item tags include Mineralogy's dynamic union,
so enabled Mineralogy rocks work in native tool recipes. NeoForge itself also
uses `c:cobblestones/normal` in several higher-priority vanilla recipe
overrides. Mineralogy therefore rebuilds that tag as well as the canonical
`c:cobblestones`, compatibility, vanilla, and Mineralogy block and item tag
membership after initial tag loading and every data reload. It preserves other
mods' members. Minecraft 26.2 retains live named holder sets rather than a
public global ingredient-cache invalidator, so
Mineralogy updates both each existing named holder set and every affected
holder's tag membership in place; the holder-set invalidation callbacks make
the recipe manager observe the new membership immediately.

Sixteen established vanilla recipes and their advancements use stable
Mineralogy union tags for the complete exact-cobblestone,
stone-crafting-material, and stone-tool-material contracts. When equivalence
is disabled those dynamically rebound tags contain vanilla materials plus
unconditional chert and pumice; enabling it adds all 27 families and safe
native aliases. The access transformer exposes only the package-private holder
binding methods needed for that targeted update; it changes no game identity
or recipe behavior by itself.

Minecraft 26.2 retains three additional configurable recipes: coast, sentry, and vex
armor-trim template duplication. They use the same dynamically rebound
Mineralogy cobblestone union. Their vanilla advancements are intentionally untouched
because those recipes unlock from owning the template, not from cobblestone.

Minecraft 26.2 also owns andesite, basalt, diorite, granite, tuff, and several
matching finishes. Mineralogy's family tags include both native and retained
legacy identities. Five `data/minecraft/recipe/polished_*.json` overrides move
the native polished-block route from 2x2 crafting to one exact native block plus
sand. That leaves 2x2 matching raw blocks available for Mineralogy bricks. The
matching vanilla advancements are overridden too, so native polishing is
revealed only after the player has both the exact native rock and sand.

Mineralogy models for exact native-equivalent raw and polished andesite,
diorite, granite, basalt, and tuff surfaces reference Minecraft's textures
directly. Basalt models preserve the native top/side distinction rather than
flattening the column texture. This keeps Mineralogy's upright slabs and other
compatible forms visually continuous with their native inputs and lets
resource packs restyle both identities together. Mineralogy-only brick
finishes and custom furnace fronts retain their own artwork.

Native `minecraft:tuff`, `minecraft:polished_tuff`, and
`minecraft:tuff_bricks` join the matching family tags only where no competing
vanilla output exists. Tuff plus sand produces vanilla polished tuff, while a
2x2 of raw tuff remains Mineralogy's brick route. Vanilla polished-tuff brick,
stair, wall, and chiseled recipes remain authoritative.
Mineralogy models in those three exact tuff families reference
`minecraft:block/tuff`, `minecraft:block/polished_tuff`, and
`minecraft:block/tuff_bricks` directly. This keeps Mineralogy's upright slabs
and other compatible forms visually continuous with native blocks.
Mineralogy's smooth tuff brick has no native equivalent and retains its own
texture.
`minecraft:smooth_basalt` joins the smooth-basalt family and can unlock and
craft Mineralogy smooth-basalt forms,
while Minecraft's basalt-to-smooth-basalt smelting recipe remains intact.
Deepslate is deliberately only an OreSpawn terrain and ore host, not a
Mineralogy construction family.

Do not broaden vanilla slab, stair, or wall recipes. Where Minecraft already
owns a matching form (raw and polished andesite/diorite/granite slabs and
stairs, plus their raw walls and the native tuff construction family), the
Mineralogy-output recipe keeps an exact legacy Mineralogy input except for the
deliberate slab overrides below. Basalt has no native construction forms, so
its safe family tags feed Mineralogy slabs, stairs, and walls. Polished
andesite/diorite/granite have
no native walls, so those three Mineralogy wall recipes may also accept the
matching family tag.

The nine shared raw/polished/tuff slab crafting recipes and fifteen
corresponding stonecutting routes deliberately output Mineralogy's
upright-capable slabs. Eighteen exact shapeless recipes provide reversible 1:1
conversion between those Mineralogy slabs and their retained vanilla
counterparts. This preserves exact
item compatibility for other mods without allowing a normal crafting or
stonecutting route to prefer the less capable slab.

Use broad `stone` or `cobblestone` inputs only when any matching material is
valid. Recipes returning a Mineralogy construction form must use the exact
material and finish so basalt cannot produce a different rock's slab or wall.

## Crafting Data

All Mineralogy recipes are native Minecraft/NeoForge 26.2 JSON under
`data/mineralogy/recipe/`. Run `scripts/generate-recipes.ps1` after changing
the recipe matrix; it generates the 27 stone families and global recipes, the
native slab/stonecutting overrides and compatibility conversions, and the five
target-native polished-block recipe/advancement overrides, then retains the
target-native smelting data. Every Mineralogy recipe has a matching unlock
advancement with the same NeoForge conditions and the same exact-item or
family-tag material predicate as the recipe. Unlocks use direct inventory
ingredients instead of listening to other recipe unlocks, which would
recursively reveal an entire construction tree. Polishing uses Minecraft
26.2's advancement requirements matrix to require the matching source plus
accepted sand; manually crafting a recipe is the target-native fallback for
Forge's delayed crafting-output inventory trigger. Rock-furnace advancements
use the matching slab-family tag as their sole material criterion. They
deliberately do not require an already-owned vanilla furnace, so the upgrade
route is visible before that intermediate is crafted.
Every generated recipe advancement, including each overridden Minecraft
advancement, explicitly sets `sends_telemetry_event` to `false`.

Reliefs preserve the historical two-stage contract. Nine matching polished
blocks produce 16 blank reliefs; a target-native synonym may satisfy the exact
rock-family tag. The blank relief is then the exact input to the marked relief
recipes. Two matching left reliefs shapelessly produce two right reliefs. Do
not substitute unrelated materials or broad stone tags for these inputs.
Do not reintroduce a parallel Java crafting registry.

## Backward Compatibility

Keep the `mineralogy` mod ID, every registry name, tile ID, NBT field, asset
path, recipe identity, patch alias, common/compatibility tag identity, and provider rule
stable. Production Java packages use `zone.moddev.mc.mineralogy`; implementation
package names are not saved-world identities.

The legacy `GENERATE_*` flags can remove registrations on the next start. The
new issue-121 switches only change creative visibility and Mineralogy-owned
recipes, so existing content remains loadable.

NeoForge 26.2 converts pre-flattening chunks lazily. Required Mixins expand
Minecraft's fixed legacy state tables before conversion, and the selected-world hook
installs the complete saved block mapping before Mojang's data fixer. It
reinstalls that mapping after the client enumerates other old saves, normalizes
legacy rock-furnace tile IDs, retains sidecar recovery, and protects populated
chunks from cross-boundary feature writes. The Mixins target exact owners and
descriptors and fail startup if a required injection point cannot be found.
Minecraft 26.2's four-argument chunk upgrader is intercepted before and after
conversion, and the discovery pass indexes both historical root `region` files
and `dimensions/minecraft/overworld/region`. The obsolete JavaScript coremod is
deliberately absent. Validate both previously
unloaded occupied furnaces and new chunks at an old-world boundary in the
reobfuscated jar; a development launch alone cannot prove this path.

## Building

The build uses NeoGradle 7.1.38 and the Gradle 9.2.1 wrapper on Java 25,
with an exact Temurin 25.0.3+9 toolchain for production and test bytecode.
NeoForge 26.2 uses the validated binary userdev path; the temporary 26.1.2
source-decompiler workaround is deliberately absent:

```powershell
$env:JAVA_HOME='path-to-Temurin-25.0.3+9'
$env:GRADLE_USER_HOME='D:\MinecraftMineralogy\.gradle-verify-cache'
.\gradlew.bat clean check build javadoc verifyReleaseConfiguration verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums --no-daemon
.\gradlew.bat eclipse verifyEclipseProductionClasspath --no-daemon
.\gradlew.bat assemble --no-daemon
```

Inspect complete client/server logs and test the reobfuscated jar with released
OreSpawn in a launcher-like NeoForge installation. The normal jar packages this
guide under `META-INF/mineralogy/docs/`.

The complete release version is `Major.Minor.Bug.Target`; see
[Mineralogy Versioning](VERSIONS.md). This branch validates target `2602002`
for Minecraft 26.2 NeoForge and does not append CI build numbers.
