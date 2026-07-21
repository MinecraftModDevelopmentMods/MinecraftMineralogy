# Mineralogy Developer Guide

## Responsibility Split

| Mineralogy owns | OreSpawn owns |
| --- | --- |
| Blocks, items, fluids, block entities | Terrain replacement and formations |
| Models, textures, language, recipes, loot | Geomes and biome influences |
| Mining, family, and crafting tags | Ore and fluid-deposit placement |
| Registry aliases and old-save compatibility | Profiles, UI, retrogen, templates |
| Mineralogy's provider declaration | Public worldgen API and provider loader |

Mineralogy has a mandatory runtime dependency on OreSpawn `[4.0.0,5.0.0)`.
Do not build integrations against Mineralogy internals. Use
`com.mcmoddev.orespawn.api` or an OreSpawn provider file.

## Packaged Provider

`src/main/resources/data/mineralogy/orespawn/provider.json` is schema 3,
provider revision 3. It currently declares 32 rock rules and three ore rules:
sulfur, phosphorous, and nitrate. Profile defaults add stable Sky formations,
Overworld stone/deepslate hosts, matching-vanilla aliases, and Mineralogy crude
oil as the provider-owned `mineralogy:fluid_deposit/crude_oil` rule.

OreSpawn owns the optional vanilla ore presets. They must not be duplicated in
Mineralogy's declaration.

When changing the declaration:

1. Keep provider-owned IDs stable.
2. Increase `provider_revision` when adding provider IDs or defaults that
   existing worlds should discover.
3. Preserve user-edited world profiles; OreSpawn merges only new provider IDs.
4. Run the provider test and an integrated worldgen smoke.

Pack authors may place an authoritative override at
`config/mineralogy-orespawn.json`. If that present file is malformed, OreSpawn
rejects the provider rather than silently using the bundled declaration.

## Useful Tags

- `mineralogy:rocks` and `mineralogy:standard_rocks` exist as block and item
  tags.
- `mineralogy:rocks/sedimentary`, `mineralogy:rocks/metamorphic`, and
  `mineralogy:rocks/igneous` expose the broad taxonomy.
- `mineralogy:crafting_materials/<rock>` item tags back matching recipes.

Use Forge and vanilla stone/cobblestone tags when broad compatibility is
intended. Keep rock-specific recipes narrow enough to return the matching
Mineralogy decorative block or furnace.

## Backward Compatibility

Mineralogy's andesite, basalt, diorite, granite, and tuff remain registered even
though new terrain aliases them to Minecraft's blocks. Registry remaps and the
legacy world-data hook must stay available for old saves. Changing a registry
ID requires a deliberate migration, not just a renamed Java field.

## Building

Use Java 17 and run:

```powershell
.\gradlew.bat test build javadoc --no-daemon
.\gradlew.bat genEclipseRuns eclipse --no-daemon
```

The normal jar packages these guides at `META-INF/mineralogy/docs/` and places
the short public agent guide at its root. Private workspace agent notes are
ignored and must not be committed.

For all provider fields, templates, custom dimensions, Java API calls, and
performance rules, use the OreSpawn guide in `config/orespawn-guide/`.
