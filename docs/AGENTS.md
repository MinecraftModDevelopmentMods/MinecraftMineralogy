# Mineralogy 6 Provider Notes

Mineralogy is an OreSpawn 4 provider and first customer. It does not own a
separate world-generation API or runtime engine.

- Required engine: OreSpawn `[4.0.0,5.0.0)`.
- Provider resource: `data/mineralogy/orespawn/provider.json`, schema 2.
- Mineralogy owns block/fluid/item registration, resources, recipes, loot,
  mining tags, and old-world registry compatibility.
- OreSpawn owns terrain, geomes, formations, ores, oil placement, profiles,
  world-creation UI, retrogen, flat bedrock, and integration APIs.
- Never restore deleted `com.mcmoddev.mineralogy.api`, `client` geology editor,
  or `worldgen` packages. Make engine changes in OreSpawn.
- Keep the provider declarative. No callbacks, registry access, logging, config
  reads, or allocations belong in generation loops.
- Matching vanilla rocks remain preferred worldgen outputs through aliases;
  Mineralogy equivalents stay registered for old saves and player choice.

For integrations, read OreSpawn's packaged root `AGENTS.md` and
`META-INF/orespawn/docs`.
