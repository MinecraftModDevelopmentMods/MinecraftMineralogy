# Mineralogy Integration Notes For Humans And Agents

Mineralogy 6.0 is a required Forge mod and world-generation engine. Public API
major version 1 consists only of `com.mcmoddev.mineralogy.api`. Treat every
other Java package as internal and unstable.

Integration entry points:

- Java declarations: `MineralogyApi.enqueue(WorldgenProvider)` during
  `InterModEnqueueEvent`.
- File declarations: `config/<modid>-mineralogy.json`, provider schema 2.
- Ore-only compatibility: provider schema 1 remains accepted.
- Active queries: `getActiveProfile(MinecraftServer)` and
  `createSampler(ServerLevel)`.
- Native-ore takeover: disable only when `isOreTakeoverActive(modid)` is true.

Configuration contracts:

- Global `config/mineralogy-geomes.json`: schema 4.
- World `serverconfig/mineralogy-geology.json`: schema 3.
- Provider `config/<modid>-mineralogy.json`: schema 2.
- JSON Schemas and examples are under `META-INF/mineralogy/docs/` in the jar.

Lifecycle and ownership:

- Forge setup is parallel. Never mutate Mineralogy internals directly.
- File definitions override API definitions for the same provider.
- Malformed present files fail closed and leave the provider inactive.
- Provider-owned rocks, ores, geomes, automatic dimensions, and templates use
  the provider namespace. Biome rules may target external biomes.
- Definitions freeze at load completion and change only after restart.

Performance constraints:

- Do not request callbacks in block-generation loops.
- Registry IDs remain `ResourceLocation` values until setup-time baking.
- Dimension, tag, alias, biome, geome, family, and block-state resolution occurs
  before generation.
- The chunk hot path must contain no config reads, registry access, strings,
  logging, or per-block allocation.

Common tasks and examples are documented in `API.md`, `PROVIDERS.md`,
`TEMPLATES.md`, and `DIMENSIONS.md`. The normal Mineralogy jar is the compile
and runtime dependency; it must not be embedded or shaded into another mod.
