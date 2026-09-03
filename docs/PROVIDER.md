# Mineralogy OreSpawn Provider

Mineralogy packages its OreSpawn declaration at:

```text
data/mineralogy/orespawn/provider.json
```

The exact bytes from the installed build are exported as
`config/mineralogy-guide/examples/mineralogy-provider.json`.

## Current Contents

- Provider schema 4, mod ID `mineralogy`, and provider revision 3.
- 32 enabled rock rules across the four geological families.
- Sulfur, phosphorous, and nitrate ore rules.
- Minecraft 1.21.1 heights `-64` through `319` and Overworld-only terrain defaults.
- Minecraft blocks for granite, diorite, andesite, basalt, and tuff. Worldgen
  aliases map the five matching historical Mineralogy rock IDs to their native
  outputs, while all historical Mineralogy blocks remain registered for old
  worlds. Native smooth basalt is also accepted by the matching smooth family.
- Both `minecraft:stone` and `minecraft:deepslate` are terrain hosts. The three
  ore rules use Minecraft's stone- and deepslate-ore-replaceable tags.
- Sky/geome Stable Layers defaults for fresh worlds plus Cyano defaults used
  when OreSpawn migrates an established Mineralogy 3 world.
- The covered `mineralogy:fluid_deposit/crude_oil` rule at Y `-48` through `48`,
  frequency `0.08`, sedimentary hosts, all nine explicit vanilla Ocean biome
  IDs, and the `OCEAN` biome-dictionary filter. Keeping both selectors lets the
  rule cover vanilla oceans precisely while remaining compatible with modded
  biomes that advertise the established Ocean classification.

Mineralogy contains no separate terrain or ore generator. OreSpawn compiles
and runs this declaration.

## Pack Override

To replace the bundled declaration for a pack, provide one complete file at:

```text
config/mineralogy-orespawn.json
```

The external file is authoritative when present. Validate it against
OreSpawn's provider schema before shipping; OreSpawn rejects a malformed
override instead of silently mixing it with packaged defaults. Mineralogy's
legacy ore migration never overwrites an existing override or established
`config/orespawn-worldgen.json`.

Existing worlds use their own profile at
`<world>/serverconfig/orespawn-worldgen.json`. Provider updates do not replace
established world choices. See `config/orespawn-guide/` for the complete schema,
field, template, dimension, API, and performance references.

When upgrading from Mineralogy 6.0, Mineralogy copies the old
`config/mineralogy-geomes.json` and per-world
`serverconfig/mineralogy-geology.json` settings into their OreSpawn equivalents
only when no OreSpawn file already exists. Old block-named Mineralogy entries
are mapped to the stable provider rule IDs so the same ore is not registered
twice. The source files are retained unchanged, migration reports are written
beside the new files, and a second start makes no further migration changes.

## Rock Altitude Defaults

Each entry in `rocks` may set a soft altitude preference with `depth_peak` and
`depth_spread`, then constrain it with the inclusive hard bounds `min_y` and
`max_y`. A larger spread keeps more of the rock's weight farther above and
below its peak. These fields combine with the rule's overall `weight`, family,
and per-geome weights; they do not guarantee that one rock occupies every block
at its preferred level.

Pack authors can establish different defaults in a complete
`config/mineralogy-orespawn.json` provider override. Players can use
OreSpawn's **Rocks & Ores...** editor while creating a world. Once a world has
been created, its saved `orespawn-worldgen.json` profile is authoritative and
provider changes do not rewrite it.

## Dimension Membership

Issue #30's dimension configuration is supplied by OreSpawn. Add the target to
`terrain_dimensions` with its eligible `host_blocks` or `host_tags`, then add
that same dimension ID to each desired rock rule's `dimensions` array. A rock
without `dimensions` is Overworld-only. A configured custom dimension is
disabled during baking when no valid rock rules include it.

Minecraft 1.21.1 uses the registered dimension-type ID, such as
`examplemod:moon`; consult the dimension mod's documentation for its exact ID.
Rock membership makes different stone sets possible per dimension. To use
different altitude settings for the same output block, define unique rock rule
IDs with different `depth_peak`, `depth_spread`, `min_y`, or `max_y` values and
non-overlapping dimension lists. Duplicate output states within one dimension
are rejected.

The 1.21.1 editor does not expose terrain-dimension or rock-membership fields, so
players edit the stopped world's saved profile and pack authors may provide
global defaults or a complete provider override. Changes affect newly generated
chunks only.

## Terrain Replacement Hosts

Issue #57's replacement blocklist is provided by OreSpawn rather than
`mineralogy-common.toml`. The packaged profile enables
`terrain_dimensions.minecraft:overworld` with `minecraft:stone` and
`minecraft:deepslate` in `host_blocks`. Pack authors may add installed
natural-terrain registry IDs or appropriate host tags to that dimension.

Use `config/orespawn-worldgen.json` for installed-pack defaults applied to new
worlds. Once a world exists, its
`<world>/serverconfig/orespawn-worldgen.json` snapshot is authoritative and
must be edited directly while the game or server is stopped. A complete
`config/mineralogy-orespawn.json` override is appropriate only when a pack
intends to replace the whole packaged Mineralogy provider; it is unnecessary
for an ordinary per-world host-list change and does not rewrite established
world profiles.

On Minecraft 1.21.1, terrain hosts are baked as flattened block identities.
Changes affect only newly generated chunks because Mineralogy strata are never
retro-generated.
