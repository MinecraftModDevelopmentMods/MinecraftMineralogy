# Mineralogy Content Configuration

Mineralogy writes `config/mineralogy-common.toml`. These settings control
registered content, recipes, drops, and old-world handling. They do not control
terrain or ore placement; use OreSpawn for worldgen.

Changes require a restart. Disabling a family may remove its recipes or
registration from the next launch, so do not disable content already present
in a world unless you understand the missing-registry consequences.

## General Options

| Key | Default | Purpose |
| --- | --- | --- |
| `patch_world` | `true` | Allow compatibility patching for older Mineralogy worlds. |
| `SMELTABLE_GRAVEL` | `true` | Allow gravel to smelt into generic stone. |
| `DROP_COBBLESTONE` | `false` | Make ordinary rock blocks also drop cobblestone. |
| `COBBLESTONE_EQUIVILENT` | `true` | Treat rocks as cobblestone equivalents where supported. The historical misspelling is the real config key. |
| `GROUP_TABS_BY_TYPE` | `false` | Split creative inventory entries by item type where supported. |

## Generated Content Flags

All of these default to `true`:

- `GENERATE_RELIEFS`
- `GENERATE_ROCKSTAIRS`, `GENERATE_ROCKFURNACE`, `GENERATE_ROCKSLAB`,
  `GENERATE_ROCKWALL`
- `GENERATE_BRICK`, `GENERATE_BRICKFURNACE`, `GENERATE_BRICKSTAIRS`,
  `GENERATE_BRICKSLAB`, `GENERATE_BRICKWALL`
- `GENERATE_SMOOTH`, `GENERATE_SMOOTHFURNACE`, `GENERATE_SMOOTHSTAIRS`,
  `GENERATE_SMOOTHSLAB`, `GENERATE_SMOOTHWALL`
- `GENERATE_SMOOTHBRICK`, `GENERATE_SMOOTHBRICKFURNACE`,
  `GENERATE_SMOOTHBRICKSTAIRS`, `GENERATE_SMOOTHBRICKSLAB`,
  `GENERATE_SMOOTHBRICKWALL`

The names use `GENERATE` for historical compatibility even though these flags
mostly control content registration and recipes, not chunk generation.
