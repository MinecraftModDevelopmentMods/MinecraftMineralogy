# Player Guide

## Installing

Mineralogy 6 needs both the Mineralogy and OreSpawn 4 jars for Minecraft
1.18.2. Install both on clients and servers. A world that contains Mineralogy
blocks should not be opened without Mineralogy.

## Creating A World

Open **OreSpawn World Generation** from the world-creation screen. Recommended
Defaults is a good starting point. The in-game **Help & Guide** explains Sky
geology, classic Cyano layers, formation sizes, ores, fluid deposits, geomes,
and biomes.

Mineralogy supplies the rock and deposit choices; OreSpawn performs the actual
generation. Changes apply to newly explored chunks only.

## What Mineralogy Adds

- Sedimentary, metamorphic, intrusive igneous, and volcanic igneous rocks.
- Rock-specific slabs, stairs, walls, bricks, polished blocks, reliefs, and
  furnaces when their content options are enabled.
- Sulfur, phosphorous, and nitrate ores.
- Rock salt lamps and streetlamps.
- Crude oil blocks and buckets. OreSpawn decides where deposits form.

Most ordinary rocks work in stone and cobblestone recipes. A furnace made from
one matching rock produces that rock's Mineralogy furnace where a matching
family exists.

## Vanilla-Named Rocks

Minecraft now has andesite, basalt, diorite, granite, and tuff. Mineralogy uses
those vanilla blocks in new layers by default. Its historical blocks remain
registered so old saves load correctly and can be selected again in a pack.

## Configuration And Servers

`mineralogy-common.toml` controls content and recipes. OreSpawn's worldgen
screen and JSON profile control generation.

Every world stores a complete profile at
`<world>/serverconfig/orespawn-worldgen.json`. Copy the world, including that
file, to a dedicated server and install the same required mods. Blocks from
missing mods cannot be generated.

For advanced pack setup, read `config/mineralogy-guide/` and
`config/orespawn-guide/` after the game has started once.
