# Mineralogy 6 Agent Guide

Mineralogy is an OreSpawn 4 content provider and first customer. Read
`DEVELOPER_GUIDE.md` and `PROVIDER.md` before changing integrations.

- Required engine: OreSpawn `[4.0.0,5.0.0)`.
- Provider resource: `data/mineralogy/orespawn/provider.json`, schema 3,
  provider revision 3.
- Mineralogy owns block, fluid, and item registration; assets; recipes; loot;
  mining and crafting tags; and old-world registry compatibility.
- OreSpawn owns terrain, geomes, formations, ores, fluid-deposit placement, profiles,
  world-creation UI, retrogen, flat bedrock, and integration APIs.
- Do not restore old `zone.moddev.mc.mineralogy.api`, geology editor, or worldgen
  packages. Make reusable engine changes in OreSpawn.
- Keep the provider declarative. No callbacks, registry access, logging, config
  reads, or allocation belong in generation loops.
- Matching vanilla rocks remain preferred worldgen outputs through aliases.
  Mineralogy equivalents stay registered for old saves and explicit pack use.
- Mineralogy contributes sulfur, phosphorous, and nitrate ore rules. OreSpawn
  owns optional vanilla-ore presets; do not duplicate them here.
- Mineralogy owns and supplies the crude-oil fluid block and the
  `mineralogy:fluid_deposit/crude_oil` provider rule. OreSpawn has no standalone
  oil block or default fluid rule.

The complete OreSpawn integration guide is packaged by OreSpawn and exported
to `config/orespawn-guide/` on first load.
