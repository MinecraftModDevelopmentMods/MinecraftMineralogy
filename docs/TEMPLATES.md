# Geology Templates

Templates are named profile overlays supplied by a provider. They may set
formation presets, rocks, geomes, biome rules, ores, oil, and terrain
dimensions. A template may reference any installed registry ID and list
`required_mods`.

Templates never activate merely because their provider is installed. Players
select one in Mineralogy's Create World screen. Dedicated servers may set
`default_template` in `config/mineralogy-geomes.json`.

Template application happens after installed-pack global configuration and
before world-creation edits. The selected result is copied into the world
profile. Provider template changes never rewrite an existing world.

Use namespaced template IDs, such as `examplemod:ancient_sea`. Translation keys
for the selector belong to the provider resource pack.
