# Mineralogy 6 With OreSpawn 4

Mineralogy 6.0 owns and registers its rocks, decorative families, furnaces,
ores, and crude-oil fluid. OreSpawn 4.0 owns terrain replacement, geomes, ore
placement, oil deposits, world profiles, configuration screens, retrogen, and
the public world-generation API.

Mineralogy has a mandatory runtime dependency on OreSpawn `[4.0.0,5.0.0)` and
packages its provider at `data/mineralogy/orespawn/provider.json`. That provider
contains Mineralogy's stable-layer defaults, 32 rock rules, 14 ore rules,
biome/geome influences, matching-vanilla aliases, and crude-oil settings.

Use `config/orespawn-worldgen.json` for installed-pack defaults and
`<world>/serverconfig/orespawn-worldgen.json` for a world's self-contained
snapshot. OreSpawn's jar contains the full API guide, schemas, examples, and
root `AGENTS.md`.

Mineralogy's `mineralogy-common.toml` contains content and recipe options only.
Older Mineralogy blocks remain registered for world compatibility even where a
matching vanilla block is now the default worldgen output.
