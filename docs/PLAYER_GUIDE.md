# Mineralogy Player Guide

## Installing

Mineralogy 6 needs Minecraft 1.18.2, Forge 40.3.0, and OreSpawn
4.0.10.118021.
Install matching Mineralogy and OreSpawn jars on both clients and servers. Do
not open a world containing Mineralogy blocks without Mineralogy installed.

## Creating Or Upgrading A World

Open **OreSpawn World Generation** while creating a world. **Recommended
Defaults** selects Mineralogy's Sky/geome Stable Layers profile. Use **Help &
Guide** for OreSpawn's explanation of geology, formations, geomes, ores, and
fluid deposits. Worldgen changes affect newly explored chunks only.

OreSpawn detects worlds created by Mineralogy 3 and keeps them on the
Cyano/`LEGACY` engine with their established family order and layer settings.
It does not silently convert old terrain to Stable Layers. Back up an important
world before changing its profile or upgrading mods.

On the first 1.18.2 start, Mineralogy recognizes the old saved registry and
protects existing Overworld chunks while Minecraft converts them to flattened
block states. Rock furnaces are converted when their chunk is first loaded;
their inventory and cooking progress are retained. Old vanilla tile IDs are
also normalized so a legacy chest or furnace cannot prevent its whole chunk
from loading. Let the game or server stop normally after the upgrade so
converted chunks can be saved. A complete copied world test is strongly
recommended before upgrading the original.

## What Mineralogy Adds

- Sedimentary, metamorphic, intrusive igneous, and volcanic igneous rocks.
- Matching slabs, stairs, walls, bricks, polished blocks, reliefs, and
  furnaces when their historical content flags are enabled.
- Sulfur, phosphorous, and nitrate ores and dusts.
- Drywall, mineral fertilizer, rock salt lamps, and street lamps.
- Distinct `mineralogy:crude_oil` and `mineralogy:flowing_crude_oil` fluids,
  plus a block and bucket. OreSpawn decides
  where covered Ocean deposits form.

Raw Mineralogy rocks work in broad stone recipes. With
`COBBLESTONE_EQUIVILENT=true`, they also join Forge's block and item
`cobblestone` tags for recipes such as levers, pistons, dispensers, and brewing stands.
Exact Mineralogy rocks still make their matching Mineralogy slabs, stairs, and
walls.

Minecraft also has its own andesite, diorite, granite, polished-andesite,
polished-diorite, and polished-granite slabs. Normal crafting and stonecutting
produce Mineralogy's upright-capable versions. If another mod requires the
exact Minecraft slab item, place one matching Mineralogy slab by itself in a
crafting grid to convert it 1:1; placing the vanilla slab by itself converts it
back without loss.

Minecraft's native basalt and tuff work as members of their matching Mineralogy
families. Native smooth basalt also works wherever Mineralogy expects smooth
basalt, while Minecraft's normal basalt-smelting recipe is left unchanged.
Deepslate is used as terrain and an ore host, but is not a Mineralogy
construction family.

Because raw rocks are cobblestone equivalents, eight raw Mineralogy rocks
around an empty crafting-grid centre make a normal Minecraft furnace. A
Mineralogy rock furnace is an upgrade recipe: surround an existing Minecraft
furnace with eight matching Mineralogy slabs. Brick, polished, and
polished-brick furnaces use the corresponding slab finish.

## Configuration And Servers

`config/mineralogy-common.toml` controls Mineralogy content and recipe compatibility.
OreSpawn's UI and JSON profile control terrain and deposits. Settings require a
restart.

### Choosing Where Each Rock Is Most Common

While creating a world, open **OreSpawn World Generation**, choose **Rocks &
Ores...**, select the appropriate rock-family tab, and then select the rock.
Its **Rock Settings** screen provides four altitude controls:

- **Preferred Y** (`depth_peak`) is the level where that rock has its strongest
  depth preference. It is a preference, not a hard placement level.
- **Depth Spread** (`depth_spread`) controls how gradually that preference
  falls away above and below Preferred Y. A larger value makes the rock common
  across a broader vertical range.
- **Minimum Y** (`min_y`) and **Maximum Y** (`max_y`) are inclusive hard limits.
  The rock cannot replace terrain outside them.

Minecraft 1.18.2 accepts Y `-64` through `319`; Depth Spread accepts `1` through
`512`. The saved fields are independent of the rock's geological family,
overall weight, and geome weights, which also affect where it is selected.

For defaults inherited by newly created worlds, the same fields can be edited
in `config/orespawn-worldgen.json`. For an established world, stop the game or
server and edit its authoritative
`<world>/serverconfig/orespawn-worldgen.json` instead. Restart after changing
the profile. Changes affect only chunks generated afterward; OreSpawn does not
retro-generate different rock strata into existing chunks.

### Upgrading a Mineralogy 6.0 World

On the first 6.1 start, existing Mineralogy 6.0 JSON geology settings are
copied into OreSpawn before its provider is loaded. Both the instance defaults
and the established world's own profile are retained, while old block-named
rock and ore rules are renamed to Mineralogy's stable provider IDs to avoid
double ore generation. Mineralogy never edits the old files and never replaces
an existing OreSpawn global or world profile. Keep the generated migration
reports until the upgraded world has been checked and reloaded successfully.

### Choosing Which Terrain Blocks Mineralogy Replaces

Mineralogy does not maintain a separate replacement blocklist. OreSpawn owns
terrain replacement through each dimension's `host_blocks` and `host_tags`.
The packaged Mineralogy profile initially uses `minecraft:stone` and
`minecraft:deepslate` in the Overworld.

The terrain-host list is not exposed by OreSpawn's 1.18.2 graphical editor. Stop
Minecraft or the server before editing the JSON. For defaults inherited by
worlds created afterward, edit:

```text
config/orespawn-worldgen.json
```

For a world that already exists, edit its authoritative saved profile instead:

```text
<world>/serverconfig/orespawn-worldgen.json
```

Find `terrain_dimensions`, retain `minecraft:stone`, and add the registry ID of
each natural terrain block that Mineralogy rock may replace:

```json
"terrain_dimensions": {
  "minecraft:overworld": {
    "enabled": true,
    "biome_ids": [],
    "biome_namespaces": [],
    "host_blocks": [
      "minecraft:stone",
      "minecraft:deepslate",
      "examplemod:custom_stone"
    ],
    "host_tags": []
  }
}
```

Use `host_tags` when an OreSpawn/pack-provided group is more appropriate; on
Minecraft 1.18.2 these resolve through target-native block tags. Exact
`host_blocks` entries are the clearest choice for one
modded stone.

Minecraft 1.18.2 matches a `host_blocks` entry by flattened block registry
identity. Add only
natural base-terrain blocks, not machines, containers, or construction blocks.
Restart after editing. The change affects only chunks generated afterward;
OreSpawn never retro-generates Mineralogy rock strata into existing chunks.

### Enabling Mineralogy Geology In Another Dimension

OreSpawn can apply Mineralogy rocks to a stone-based mod dimension without a
separate Mineralogy dimension option. Minecraft 1.18.2 uses the dimension
type's registered ID. For example, use `examplemod:moon` when that is the ID
documented by the installed dimension mod.

The 1.18.2 graphical editor does not expose terrain-dimension or rock-membership
fields. Stop Minecraft or the server and edit the appropriate OreSpawn JSON:
`config/orespawn-worldgen.json` for defaults inherited by future worlds, or
`<world>/serverconfig/orespawn-worldgen.json` for an established world.

First, add an enabled entry inside `terrain_dimensions`. This example allows
Mineralogy to replace vanilla stone throughout a custom moon dimension:

```json
"examplemod:moon": {
  "enabled": true,
  "biome_ids": [],
  "biome_namespaces": [],
  "host_blocks": ["minecraft:stone"],
  "host_tags": []
}
```

Use the dimension's actual base-stone registry ID instead when it does not use
vanilla stone. Optional `biome_ids` or `biome_namespaces` can limit replacement
within the dimension.

Second, add the dimension to every desired entry under `rocks`:

```json
"dimensions": ["minecraft:overworld", "examplemod:moon"]
```

Keep `minecraft:overworld` in the list when that rule should continue to work
there. A rock entry with no `dimensions` field is Overworld-only, and OreSpawn
disables a custom terrain dimension if none of the configured rocks include it.
Different dimensions can therefore use different rock sets by assigning
different membership lists.

For advanced packs, the same output block may have separate rules with unique
rule IDs, different altitude fields, and non-overlapping `dimensions` lists.
This allows, for example, basalt to use different Preferred Y or hard height
bounds in the Overworld and a planet dimension. Do not make both rules eligible
in the same dimension, because duplicate output states are rejected while that
dimension is baked.

Restart after editing and inspect the OreSpawn startup log to confirm that the
custom dimension baked with eligible rocks and resolved host blocks. Only new
chunks receive the geology; existing terrain is not retro-generated.

Each world stores its complete worldgen profile at:

```text
<world>/serverconfig/orespawn-worldgen.json
```

Copy the complete world, including that file, to a dedicated server and
install the same required mods. For advanced setup, read
`config/mineralogy-guide/` and `config/orespawn-guide/` after one start.
