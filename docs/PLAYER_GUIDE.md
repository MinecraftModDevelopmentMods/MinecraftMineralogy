# Mineralogy Player Guide

## Installing

Mineralogy 6 needs Minecraft 1.10.2, Forge 12.18.3.2511, and OreSpawn 4.0.6.
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

## What Mineralogy Adds

- Sedimentary, metamorphic, intrusive igneous, and volcanic igneous rocks.
- Matching slabs, stairs, walls, bricks, polished blocks, reliefs, and
  furnaces when their historical content flags are enabled.
- Sulfur, phosphorous, and nitrate ores and dusts.
- Drywall, mineral fertilizer, rock salt lamps, and street lamps.
- A distinct `mineralogy_crude_oil` fluid, block, and bucket. OreSpawn decides
  where covered Ocean deposits form.

Raw Mineralogy rocks work in broad stone recipes. With
`COBBLESTONE_EQUIVILENT=true`, they also work in compatible Ore Dictionary
`cobblestone` recipes such as levers, pistons, dispensers, and brewing stands.
Exact Mineralogy rocks still make their matching Mineralogy slabs, stairs, and
walls.

## Configuration And Servers

`config/mineralogy.cfg` controls Mineralogy content and recipe compatibility.
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

Minecraft 1.10 accepts Y `0` through `255`; Depth Spread accepts `1` through
`512`. The saved fields are independent of the rock's geological family,
overall weight, and geome weights, which also affect where it is selected.

For defaults inherited by newly created worlds, the same fields can be edited
in `config/orespawn-worldgen.json`. For an established world, stop the game or
server and edit its authoritative
`<world>/serverconfig/orespawn-worldgen.json` instead. Restart after changing
the profile. Changes affect only chunks generated afterward; OreSpawn does not
retro-generate different rock strata into existing chunks.

### Choosing Which Terrain Blocks Mineralogy Replaces

Mineralogy does not maintain a separate replacement blocklist. OreSpawn owns
terrain replacement through each dimension's `host_blocks` and `host_tags`.
The packaged Mineralogy profile initially uses `minecraft:stone` in the
Overworld.

The terrain-host list is not exposed by OreSpawn's 1.10 graphical editor. Stop
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
      "examplemod:custom_stone"
    ],
    "host_tags": []
  }
}
```

Use `host_tags` when an OreSpawn/pack-provided group is more appropriate; on
Minecraft 1.10 these resolve through OreSpawn's compatibility mapping and Ore
Dictionary names. Exact `host_blocks` entries are the clearest choice for one
modded stone.

Minecraft 1.10 matches a `host_blocks` entry by block registry identity, not by
metadata, so every metadata state of that block becomes eligible. Add only
natural base-terrain blocks, not machines, containers, or construction blocks.
Restart after editing. The change affects only chunks generated afterward;
OreSpawn never retro-generates Mineralogy rock strata into existing chunks.

Each world stores its complete worldgen profile at:

```text
<world>/serverconfig/orespawn-worldgen.json
```

Copy the complete world, including that file, to a dedicated server and
install the same required mods. For advanced setup, read
`config/mineralogy-guide/` and `config/orespawn-guide/` after one start.
