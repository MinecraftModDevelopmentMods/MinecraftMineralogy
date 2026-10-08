[![Discord](https://img.shields.io/badge/Discord-MMD-green.svg?style=flat&logo=Discord)](https://discord.moddev.zone)
[![CurseForge downloads](https://cf.way2muchnoise.eu/full_minecraft-mineralogy_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/minecraft-mineralogy)
[![Supported Minecraft versions](https://cf.way2muchnoise.eu/versions/Minecraft_minecraft-mineralogy_all.svg)](https://www.curseforge.com/minecraft/mc-mods/minecraft-mineralogy)
[![Build, test, and audit](https://github.com/MinecraftModDevelopmentMods/MinecraftMineralogy/actions/workflows/ci.yml/badge.svg?branch=master-26.3-neo)](https://github.com/MinecraftModDevelopmentMods/MinecraftMineralogy/actions/workflows/ci.yml?query=branch%3Amaster-26.3-neo)

# Mineralogy 6 for Minecraft 26.3

Mineralogy adds real-world rock families, matching construction blocks,
mineral ores and dusts, rock furnaces, drywall, rock-salt lighting, fertilizer,
and crude oil. OreSpawn 4 is the sole terrain, strata, ore, and deposit engine;
Mineralogy no longer installs a parallel world generator.

This branch builds Mineralogy `6.1.4.2603002` for NeoForge `26.3.0.1-beta`, is
runtime-validated on NeoForge `26.3.0.8-beta`, and is built and tested against
OreSpawn `4.0.16.2603002`. Its declared compatibility range is
OreSpawn `[4.0.6,5.0.0)`. Install both mods on clients and servers.

This is the NeoForge build. It is intentionally maintained separately from the
Forge 26.3 branch; use the jar that matches the loader in your modpack.

Mineralogy 6.1.4 restores sculk conversion, moss and dripstone decoration, and
natural animal behaviour on Mineralogy terrain. Sulfur cubes can absorb the
31 raw Mineralogy rocks, not polished blocks, bricks or other crafted items;
vanilla blocks keep Minecraft's own rules. Goats spawn on natural rocks and
break horns against the harder raw rocks. Cats can sit on lit rock furnaces,
and rock-salt lamps retain straight-wall posts like vanilla torches.

These block and animal fixes work in existing worlds. Improved cave, root and
forest decoration needs newly generated chunks. Natural terrain eligibility is
independent of cobblestone equivalence. Construction tags tolerate disabled
blocks, and slabs, stairs, furnaces, ores, storage blocks and coloured drywall
join their appropriate common interfaces. The released 6.1.3 NeoForge loader
correction and synchronized native textures are retained.

Natural bats can spawn above Mineralogy terrain under Minecraft's normal dark,
below-surface conditions. The additive bat tag applies to existing worlds and
is independent of cobblestone equivalence; crafted construction blocks remain
excluded.

## Configuration and help

Mineralogy's content and recipe switches remain in
`config/mineralogy-common.toml`. Use OreSpawn's world-creation UI or saved world
profile for rock, ore, fluid, dimension, altitude, and terrain-host settings.
Generation changes apply to new chunks only.

After the first start, the complete human guide is available under
`config/mineralogy-guide/`. The maintained source is in [docs](docs/README.md)
and covers upgrades, content controls, pack overrides, provider data, and
four-component release versions.

## Compatibility

The `mineralogy` mod ID and historical block, item, tile, NBT, recipe, asset,
patch, and pre-flattening world-conversion identities are retained. Existing
Mineralogy 5 configuration is read without being rewritten. Established worlds
continue through OreSpawn's migrated Cyano/geome profile unless their owner
explicitly selects a different engine.

[CurseForge](https://www.curseforge.com/minecraft/mc-mods/minecraft-mineralogy) ·
[Source and issues](https://github.com/MinecraftModDevelopmentMods/MinecraftMineralogy) ·
[MMD Discord](https://discord.moddev.zone)
