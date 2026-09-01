[![Discord](https://img.shields.io/badge/Discord-MMD-green.svg?style=flat&logo=Discord)](https://discord.moddev.zone)
[![CurseForge downloads](https://cf.way2muchnoise.eu/full_minecraft-mineralogy_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/minecraft-mineralogy)
[![Supported Minecraft versions](https://cf.way2muchnoise.eu/versions/Minecraft_minecraft-mineralogy_all.svg)](https://www.curseforge.com/minecraft/mc-mods/minecraft-mineralogy)
[![Build, test, and audit](https://github.com/MinecraftModDevelopmentMods/MinecraftMineralogy/actions/workflows/ci.yml/badge.svg?branch=master-1.20.1)](https://github.com/MinecraftModDevelopmentMods/MinecraftMineralogy/actions/workflows/ci.yml?query=branch%3Amaster-1.20.1)

# Mineralogy 6 for Minecraft 1.20.1

Mineralogy adds real-world rock families, matching construction blocks,
mineral ores and dusts, rock furnaces, drywall, rock-salt lighting, fertilizer,
and crude oil. OreSpawn 4 is the sole terrain, strata, ore, and deposit engine;
Mineralogy no longer installs a parallel world generator.

This branch builds Mineralogy `6.1.1.120011` for Forge `47.4.10` and is built
and tested against OreSpawn `4.0.16.120011`. Its declared compatibility range is
OreSpawn `[4.0.6,5.0.0)`. Install both mods on clients and servers.

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
