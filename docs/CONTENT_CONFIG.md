# Mineralogy Content Configuration

Mineralogy writes content options to `config/mineralogy.cfg`. Changes require
a restart. OreSpawn owns terrain, rock, ore, and fluid-deposit placement; its
world settings are separate from these content and recipe switches.

To add modded terrain blocks that Mineralogy rock may replace, do not add a
Mineralogy option. Configure OreSpawn's `terrain_dimensions` host list as
described under **Choosing Which Terrain Blocks Mineralogy Replaces** in the
[Player Guide](PLAYER_GUIDE.md).

## General options

| Key | Default | Purpose |
| --- | --- | --- |
| `patch_world` | `true` | Preserve compatibility placeholders for older Mineralogy worlds. |
| `SMELTABLE_GRAVEL` | `true` | Allow gravel to be smelted into generic stone. |
| `DROP_COBBLESTONE` | `false` | Make an ordinary raw rock drop one vanilla cobblestone instead of itself. |
| `COBBLESTONE_EQUIVILENT` | `true` | Treat raw rocks as cobblestone in compatible recipes. The misspelling is the real historical key. |
| `GROUP_TABS_BY_TYPE` | `false` | Split creative inventory entries into Rocks, Stairs, Slabs, Walls, and Items tabs. |

`GROUP_TABS_BY_TYPE` changes only creative organization. It never changes
registration, recipes, drops, or world generation. With the default `false`,
Mineralogy retains its single searchable tab and existing vanilla-tab
placements such as mineral fertilizer under Materials. With `true`, reliefs,
furnaces, lamps, drywall, storage blocks, dusts, fertilizer, and the crude-oil
bucket use Mineralogy Items.

## Compatibility-safe content switches

These four keys default to `true`:

| Key | What `false` does |
| --- | --- |
| `ENABLE_DRYWALLS` | Hides all 16 drywalls and omits their base and dye recipes. |
| `ENABLE_ROCK_SALT_LAMPS` | Hides both rock salt lights and omits their recipes. |
| `ENABLE_MINERAL_DUSTS` | Hides sulfur, phosphorous, and nitrate dusts plus their storage blocks; omits storage conversions and Mineralogy's gunpowder recipes. |
| `ENABLE_MINERAL_FERTILIZER` | Hides mineral fertilizer and omits its recipe. |

These switches never unregister content. Existing blocks, inventory stacks,
drops, Ore Dictionary entries, and integrations remain valid. The dust and
fertilizer switches are independent, so enabled fertilizer may use dusts from
existing ore, OreSpawn-generated ore, or another mod.

A clean installation writes these keys. Mineralogy deliberately does not save
an existing `mineralogy.cfg`, so missing keys retain their documented defaults.
To change them in an older file, add entries to its existing `options` block:

```text
options {
    B:COBBLESTONE_EQUIVILENT=false
    B:ENABLE_DRYWALLS=false
    B:ENABLE_MINERAL_DUSTS=false
    B:ENABLE_MINERAL_FERTILIZER=false
    B:ENABLE_ROCK_SALT_LAMPS=false
    B:GROUP_TABS_BY_TYPE=true
}
```

## Rock recipe compatibility

With `COBBLESTONE_EQUIVILENT=true`, all 27 raw Mineralogy rock families,
including rock salt, participate in Ore Dictionary `cobblestone` recipes such
as levers, pistons, dispensers, droppers, and brewing stands. Setting it to
`false` removes only that broad identity. Rocks remain `stone`, retain their
material-specific identities, make stone tools and furnaces, and use exact
Mineralogy slab, stair, and wall recipes. Chert and pumice remain historical
unconditional cobblestone equivalents.

## Disabling Mineralogy ores

Mineralogy does not provide a second ore-generation switch. In OreSpawn's
world settings, open **Rocks & Ores...**, select **Ores**, and disable these
rules:

- `mineralogy:ore/mineralogy/sulfur_ore`
- `mineralogy:ore/mineralogy/phosphorous_ore`
- `mineralogy:ore/mineralogy/nitrate_ore`

Disabling a rule affects newly generated chunks; it does not remove existing
ore. If mineral dust recipes are disabled but these rules remain enabled, the
new ore retains its historical dust drops.

## Historical generated-content flags

The following historical keys default to `true`: `GENERATE_RELIEFS`,
`GENERATE_ROCKSTAIRS`, `GENERATE_ROCKSLAB`, `GENERATE_ROCK_WALL`,
`GENERATE_ROCKFURNACES`, `GENERATE_BRICK`, `GENERATE_BRICKSTAIRS`,
`GENERATE_BRICKSLAB`, `GENERATE_BRICK_WALL`, `GENERATE_BRICKFURNACES`,
`GENERATE_SMOOTH`, `GENERATE_SMOOTHSTAIRS`, `GENERATE_SMOOTHSLAB`,
`GENERATE_SMOOTH_WALL`, `GENERATE_SMOOTHFURNACES`, `GENERATE_SMOOTHBRICK`,
`GENERATE_SMOOTHBRICKSTAIRS`, `GENERATE_SMOOTHBRICKSLAB`,
`GENERATE_SMOOTHBRICK_WALL`, and `GENERATE_SMOOTHBRICKFURNACES`.

Despite their names, these control construction-content registration and
recipes rather than chunk generation. Disabling them can make existing content
unavailable on the next start; back up a world before changing them.
