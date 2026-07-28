# Minecraft Mineralogy 6

Mineralogy adds real-world rock types to Minecraft 1.19.4, together with
matching slabs, stairs, walls, bricks, polished blocks, reliefs, and furnaces.
It also supplies sulfur, phosphorous, nitrate, rock salt lighting, and crude
oil content.

Mineralogy 6 requires **OreSpawn 4**. Mineralogy owns the blocks, items,
recipes, loot, textures, tags, and old-world compatibility. OreSpawn owns
terrain replacement, geological regions, formations, ore and fluid-deposit placement,
world profiles, configuration screens, retrogen, and the public worldgen API.

## Players

Install Mineralogy 6.1.0 and OreSpawn 4.0.1 (or a later compatible 4.x build)
for Minecraft 1.19.4 on the
client and server. When creating a world, open **OreSpawn World Generation** to
choose the recommended settings or tune the geology. Its **Help & Guide**
button explains the controls in game.

Mineralogy rocks work as stone or cobblestone where appropriate. Matching
rocks can also make their own furnaces and decorative block families.

Five names now have vanilla equivalents: andesite, basalt, diorite, granite,
and tuff. New terrain uses the vanilla blocks by default. Mineralogy's versions
remain registered so old worlds still load and pack authors can select them.

## Configuration

- `config/mineralogy-common.toml` controls Mineralogy content and recipes.
- `config/orespawn-worldgen.json` controls installed-pack worldgen defaults.
- `<world>/serverconfig/orespawn-worldgen.json` is the world's complete
  snapshot and can be copied with the world to a dedicated server.
- `config/mineralogy-orespawn.json` may override Mineralogy's packaged OreSpawn
  provider for a modpack.
- `config/mineralogy-guide/` is written on first load with Mineralogy's player,
  content, and provider documentation.
- `config/orespawn-guide/` contains the full engine, API, schema, template, and
  dimension documentation.

Configuration changes affect newly generated chunks. Existing terrain is not
rewritten.

## Developers

Start with [the developer guide](docs/DEVELOPER_GUIDE.md) and
[provider notes](docs/PROVIDER.md). Integrations should use OreSpawn's supported
API and provider format rather than calling Mineralogy internals.

Mineralogy requires Java 17. Build from the repository root with:

```powershell
.\gradlew.bat build --no-daemon
```

Private `AGENTS.md` and `agent-notes/` files are local workspace context and are
ignored. Tracked public guidance lives under `docs/` and is packaged in the jar.

## Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/minecraft-mineralogy)
- [Issues](https://github.com/SkyBlade1978/MinecraftMineralogy/issues)
- [Minecraft Mod Development Discord](https://discord.mcmoddev.com)
