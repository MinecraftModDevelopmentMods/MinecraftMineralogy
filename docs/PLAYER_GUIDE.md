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

Each world stores its complete worldgen profile at:

```text
<world>/serverconfig/orespawn-worldgen.json
```

Copy the complete world, including that file, to a dedicated server and
install the same required mods. For advanced setup, read
`config/mineralogy-guide/` and `config/orespawn-guide/` after one start.
