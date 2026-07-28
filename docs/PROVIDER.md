# Mineralogy OreSpawn Provider

Mineralogy packages its provider at:

```text
data/mineralogy/orespawn/provider.json
```

The file is also exported as
`config/mineralogy-guide/examples/mineralogy-provider.json` so pack authors can
inspect the exact declaration used by their build.

## Current Contents

- Schema version 3, provider mod ID `mineralogy`, provider revision 3.
- 32 enabled rock rules covering Mineralogy's geological families.
- Sulfur, phosphorous, and nitrate ore rules.
- Overworld replacement hosts for Minecraft stone and deepslate.
- Stable-layer profile defaults and one provider-owned
  `mineralogy:fluid_deposit/crude_oil` rule.
- Ocean-only sedimentary hosts, Y -48 to 48, covered lobe geometry, and a
  minimum two-block solid cap for crude oil.
- Worldgen aliases from Mineralogy andesite, basalt, diorite, granite, and tuff
  to their matching vanilla blocks.

The old Mineralogy blocks remain registered; aliases change only the default
block state chosen for newly generated terrain.

## Pack Override

To replace Mineralogy's bundled declaration for a pack, put a complete file at:

```text
config/mineralogy-orespawn.json
```

The external file is authoritative when present. Validate it against the
OreSpawn schema before shipping. A malformed or incomplete override makes the
Mineralogy provider inactive, allowing failures to be visible instead of
silently mixing two configurations.

Existing worlds use their own self-contained OreSpawn profile. Provider updates
can add newly introduced IDs, but do not overwrite established world choices.

The authoritative provider field reference, JSON Schema, validated example,
ownership rules, lifecycle, and Java API are in `config/orespawn-guide/`.
