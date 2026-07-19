# Configuration

Mineralogy uses three JSON contracts:

| File | Schema | Purpose |
|---|---:|---|
| `config/mineralogy-geomes.json` | 4 | Installed-pack defaults |
| `<world>/serverconfig/mineralogy-geology.json` | 3 | Self-contained world snapshot |
| `config/<modid>-mineralogy.json` | 2 | Optional provider contribution |

The effective profile for a new world is assembled in this order: Mineralogy
factory defaults, installed provider additions, global pack configuration, the
selected template, then edits made in Create World. The result is saved into
the world. Existing chunks are never rewritten.

The global and world files can configure formations, rock definitions, geomes,
biome rules, terrain dimensions, ores, oil, aliases, and provider metadata.
Registry references use full IDs such as `minecraft:calcite`.

Copying `serverconfig/mineralogy-geology.json` into the same location in a
dedicated-server world reproduces the profile when the server has all required
mods and registry entries. Restart after editing. Changes affect newly
generated chunks only.

See `schemas/mineralogy-global.schema.json`,
`schemas/mineralogy-world.schema.json`, and the files in `examples/`.
