# Mineralogy 6 for Minecraft 1.12.2

Mineralogy adds real-world rock families, matching construction blocks,
mineral ores and dusts, rock furnaces, drywall, rock-salt lighting, fertilizer,
and crude oil. OreSpawn 4 is the sole terrain, strata, ore, and deposit engine;
Mineralogy no longer installs a parallel world generator.

This branch builds Mineralogy `6.0.1.112021` for Forge `14.23.5.2859` and
requires OreSpawn `4.0.7` or another compatible 4.x release. Install both mods
on clients and servers.

## Configuration and help

Mineralogy's content and recipe switches remain in `config/mineralogy.cfg`.
Use OreSpawn's world-creation UI or saved world profile for rock, ore, fluid,
dimension, altitude, and terrain-host settings. Generation changes apply to
new chunks only.

After the first start, the complete human guide is available under
`config/mineralogy-guide/`. The maintained source is in [docs](docs/README.md)
and covers upgrades, content controls, pack overrides, provider data, and
four-component release versions.

## Compatibility

The `mineralogy` mod ID and historical block, item, tile, NBT, recipe, asset,
patch, and OreDictionary identities are retained. Established Mineralogy 3
worlds are migrated through OreSpawn's Cyano/Legacy profile unless their owner
explicitly selects a different engine.

[CurseForge](https://www.curseforge.com/minecraft/mc-mods/minecraft-mineralogy) ·
[Source and issues](https://github.com/MinecraftModDevelopmentMods/MinecraftMineralogy) ·
[MMD Discord](https://discord.mcmoddev.com)
