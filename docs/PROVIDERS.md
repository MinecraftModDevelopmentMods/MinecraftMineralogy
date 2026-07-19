# Worldgen Providers

Provider mods may contribute through Forge IMC or through
`config/<provider-modid>-mineralogy.json`. If both are present, the file is
authoritative. A malformed present file leaves that provider inactive and the
API contribution is not used as a fallback.

Schema 2 supports `rocks`, `ores`, `geomes`, `biome_rules`,
`terrain_dimensions`, and `templates`. Ore-only schema 1 files remain valid.
Each file requires a matching `provider_modid`, a positive
`provider_revision`, and at least one contribution.

Provider-owned rocks, ores, geomes, terrain dimensions, and templates must use
the provider namespace. Biome rules may target any installed biome ID. Rock,
ore, and template profiles may reference arbitrary installed blocks where the
contract allows it.

An enabled ore dimension requires a Y range, expected attempts per chunk in
`frequency`, a block budget in `quantity`, and at least one host family, host
block, or host tag. Integer frequency is guaranteed attempts; the fractional
part is the chance of one additional attempt.

Only suppress a provider mod's native ore generation when
`MineralogyApi.isOreTakeoverActive(modid)` returns true. `PENDING` means
Mineralogy has not frozen provider discovery yet. `INACTIVE` is the fail-safe:
the provider must retain native generation.

See `examples/examplemod-mineralogy.json` for rocks, an ore, a custom dimension,
and a selectable template.
