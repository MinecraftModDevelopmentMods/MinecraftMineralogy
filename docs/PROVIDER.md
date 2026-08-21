# Mineralogy OreSpawn Provider

Mineralogy packages its OreSpawn declaration at:

```text
assets/mineralogy/orespawn/provider.json
```

The exact bytes from the installed build are exported as
`config/mineralogy-guide/examples/mineralogy-provider.json`.

## Current Contents

- Provider schema 4, mod ID `mineralogy`, and provider revision 3.
- 32 enabled rock rules across the four geological families.
- Sulfur, phosphorous, and nitrate ore rules.
- Minecraft 1.10 heights `0` through `255` and Overworld-only terrain defaults.
- Minecraft stone metadata `1`, `3`, and `5` for granite, diorite, and
  andesite; Mineralogy blocks for basalt and tuff because Minecraft 1.10 does
  not supply them.
- Sky/geome Stable Layers defaults for fresh worlds plus Cyano defaults used
  when OreSpawn migrates an established Mineralogy 3 world.
- The covered `mineralogy:fluid_deposit/crude_oil` rule at Y `0` through `48`,
  frequency `0.08`, Ocean biomes, and sedimentary hosts.

Mineralogy contains no separate terrain or ore generator. OreSpawn compiles
and runs this declaration.

## Pack Override

To replace the bundled declaration for a pack, provide one complete file at:

```text
config/mineralogy-orespawn.json
```

The external file is authoritative when present. Validate it against
OreSpawn's provider schema before shipping; OreSpawn rejects a malformed
override instead of silently mixing it with packaged defaults. Mineralogy's
legacy ore migration never overwrites an existing override or established
`config/orespawn-worldgen.json`.

Existing worlds use their own profile at
`<world>/serverconfig/orespawn-worldgen.json`. Provider updates do not replace
established world choices. See `config/orespawn-guide/` for the complete schema,
field, template, dimension, API, and performance references.

## Terrain Replacement Hosts

Issue #57's replacement blocklist is provided by OreSpawn rather than
`mineralogy.cfg`. The packaged profile enables
`terrain_dimensions.minecraft:overworld` with `minecraft:stone` in
`host_blocks`. Pack authors may add installed natural-terrain registry IDs or
appropriate host tags to that dimension.

Use `config/orespawn-worldgen.json` for installed-pack defaults applied to new
worlds. Once a world exists, its
`<world>/serverconfig/orespawn-worldgen.json` snapshot is authoritative and
must be edited directly while the game or server is stopped. A complete
`config/mineralogy-orespawn.json` override is appropriate only when a pack
intends to replace the whole packaged Mineralogy provider; it is unnecessary
for an ordinary per-world host-list change and does not rewrite established
world profiles.

On Minecraft 1.10, terrain hosts are baked as block identities. Listing a
block therefore opts in all of its metadata states. Changes affect only newly
generated chunks because Mineralogy strata are never retro-generated.
