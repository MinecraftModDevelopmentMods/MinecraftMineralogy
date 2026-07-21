# Ore Provider Migration Note

Mineralogy no longer runs a general-purpose ore-provider system. OreSpawn 4
owns ore and terrain provider discovery, validation, placement, configuration,
and its public API.

Mineralogy itself contributes a packaged schema-3 provider at
`data/mineralogy/orespawn/provider.json`. See [docs/PROVIDER.md](docs/PROVIDER.md)
for Mineralogy-specific details.

To add ores or terrain from another mod, use OreSpawn's provider and API guides.
They are packaged in the OreSpawn jar and exported on first load to
`config/orespawn-guide/`.
