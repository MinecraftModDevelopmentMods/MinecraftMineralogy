# Mineralogy Content Configuration

Mineralogy writes content options to `config/mineralogy.cfg`. Changes require
a restart. OreSpawn owns terrain, rock, ore, and fluid-deposit placement; its
world settings are separate from these content switches.

## Compatibility-safe content switches

The following keys are in the `options` category and default to `true`:

| Key | What `false` does |
| --- | --- |
| `ENABLE_DRYWALLS` | Hides all 16 drywalls from creative tabs and omits their base and dye recipes. |
| `ENABLE_ROCK_SALT_LAMPS` | Hides the rock salt lamp and street lamp and omits both recipes. |
| `ENABLE_MINERAL_DUSTS` | Hides sulfur, phosphorous, and nitrate dusts and their storage blocks; omits their storage conversions and Mineralogy's three gunpowder recipes. |
| `ENABLE_MINERAL_FERTILIZER` | Hides mineral fertilizer and omits its recipe. |

These switches never unregister content. Existing blocks, inventory stacks,
drops, Ore Dictionary entries, and integrations remain valid. An existing
fertilizer item still works when its option is disabled. The dust and
fertilizer switches are independent, so enabled fertilizer may still use dusts
obtained from existing ore, OreSpawn-generated ore, or another mod.

A clean installation writes all four content keys plus the separate recipe-
compatibility key described below. Mineralogy deliberately does not rewrite an
existing `mineralogy.cfg`; missing keys use the enabled default. To disable
content in an older file, add the required entries to its existing `options`
block:

```text
options {
    B:ENABLE_DRYWALLS=false
    B:ENABLE_MINERAL_DUSTS=false
    B:ENABLE_MINERAL_FERTILIZER=false
    B:ENABLE_ROCK_SALT_LAMPS=false
}
```

## Rock recipe compatibility

`COBBLESTONE_EQUIVILENT` is a historical `options` key whose spelling is
preserved for compatibility. It defaults to `true`. When enabled, all 27 raw
Mineralogy rock families, including rock salt, participate in Ore Dictionary
`cobblestone` recipes such as levers, pistons, dispensers, droppers, and brewing
stands. Forge 1.10 already generalizes those applicable vanilla recipes, so
Mineralogy does not replace or remove them.

Setting the key to `false` removes only that broad recipe identity after a
restart. The rocks remain registered as `stone`, retain their material-specific
Ore Dictionary names, still make stone tools and furnaces, and still use their
exact Mineralogy slab, stair, and wall recipes. Chert and pumice retain their
historical unconditional `cobblestone` identity.

An existing configuration that does not contain the key remains byte-unchanged
and uses the enabled default. To opt out, add this line to its existing
`options` block:

```text
options {
    B:COBBLESTONE_EQUIVILENT=false
}
```

## Disabling Mineralogy ores

Mineralogy does not provide a second ore-generation switch. In OreSpawn's
world settings, open **Rocks & Ores...**, select the **Ores** tab, open each of
these rules, and turn **Enabled** off:

- `mineralogy:ore/mineralogy/sulfur_ore`
- `mineralogy:ore/mineralogy/phosphorous_ore`
- `mineralogy:ore/mineralogy/nitrate_ore`

OreSpawn stores the final settings with the world. Disabling a rule affects
newly generated chunks; it does not remove existing ore blocks or rewrite old
terrain. On a dedicated server, the equivalent settings live in
`<world>/serverconfig/orespawn-worldgen.json`. Existing per-world profiles and
explicit OreSpawn provider/global overrides remain authoritative.

If mineral dusts are disabled in Mineralogy but these OreSpawn rules remain
enabled, newly placed ore still has its historical dust drops. Disable both
the Mineralogy dust recipes and the corresponding OreSpawn ore rules when a
pack should provide neither new ore nor the Mineralogy processing chain.

## Historical generated-content flags

The older `GENERATE_*` names are retained for configuration compatibility.
Despite the name, most control derived construction content and recipes rather
than chunk generation. Their historical registration behavior is unchanged in
this release.
