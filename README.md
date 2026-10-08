[![Discord](https://img.shields.io/badge/Discord-MMD-green.svg?style=flat&logo=Discord)](https://discord.moddev.zone)
[![CurseForge downloads](https://cf.way2muchnoise.eu/full_minecraft-mineralogy_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/minecraft-mineralogy)
[![Supported Minecraft versions](https://cf.way2muchnoise.eu/versions/Minecraft_minecraft-mineralogy_all.svg)](https://www.curseforge.com/minecraft/mc-mods/minecraft-mineralogy)
[![Build, test, and audit](https://github.com/MinecraftModDevelopmentMods/MinecraftMineralogy/actions/workflows/ci.yml/badge.svg?branch=master-26.1.2-neo)](https://github.com/MinecraftModDevelopmentMods/MinecraftMineralogy/actions/workflows/ci.yml?query=branch%3Amaster-26.1.2-neo)

# Mineralogy 6 for Minecraft 26.1.2

Mineralogy adds real-world rock families, matching construction blocks,
mineral ores and dusts, rock furnaces, drywall, rock-salt lighting, fertilizer,
and crude oil. OreSpawn 4 is the sole terrain, strata, ore, and deposit engine;
Mineralogy no longer installs a parallel world generator.

This branch builds Mineralogy `6.1.4.2601022` for NeoForge `26.1.2.94` and is built
and tested against OreSpawn `4.0.16.2601022`. Its declared compatibility range is
OreSpawn `[4.0.6,5.0.0)`. Install both mods on clients and servers.

This is the NeoForge build. It is intentionally maintained separately from the
Forge 26.1.2 branch; use the jar that matches the loader in your modpack.

Mineralogy 6.1.4 restores sculk catalyst conversion, moss growth and dripstone
decoration on natural Mineralogy terrain. Goats can spawn on natural rocks and
break horns against harder raw rocks. Root and forest-rock decoration recognise
natural substrates, without making bare rock suitable for growing azalea trees.
Rock-salt lamps retain the centre post on straight walls.

These block and animal fixes work in existing worlds. Improved cave, root and
forest decoration needs newly generated chunks. Terrain eligibility is
independent of cobblestone equivalence; crafted blocks stay outside natural
replacement. Single/upright slabs and stairs join the vanilla aggregates;
separate double slabs remain full blocks. Construction tags, recipes, unlocks
and loot tables tolerate forms disabled by content settings. Enabled recipes
and drops are unchanged. Sulfur-cube compatibility is only for 26.2+.

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
