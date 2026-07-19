# Java API

Only `com.mcmoddev.mineralogy.api` is supported API. Every other package is an
implementation detail. API major version is available as
`MineralogyApi.API_VERSION` and in the jar manifest as
`Mineralogy-API-Version`.

Provider mods must depend on the full Mineralogy mod at compile time and
runtime. In `mods.toml` use a mandatory dependency, for example:

```toml
[[dependencies.examplemod]]
modId="mineralogy"
mandatory=true
versionRange="[6.0.0,7.0.0)"
ordering="AFTER"
side="BOTH"
```

Submit declarations during `InterModEnqueueEvent`:

```java
WorldgenProvider provider = WorldgenProvider.builder("examplemod", 1)
    .rock(new ResourceLocation("examplemod", "slate"), GeologyFamily.METAMORPHIC, rock -> rock
        .depth(12, 36)
        .weight(1.2)
        .oreReplaceable(true))
    .build();
MineralogyApi.enqueue(provider);
```

Definitions are immutable after `build()`. Registry references remain
`ResourceLocation` values until Mineralogy validates and bakes them. Provider
messages are processed through Forge IMC and frozen at load completion; direct
cross-mod mutation during parallel setup is unsupported.

Formation and oil settings use the same declarative style when building a
template:

```java
FormationDefinition formations = FormationDefinition.builder()
    .horizontalSize(FormationPreset.HUGE)
    .waviness(FormationPreset.LARGE)
    .build();
OilDefinition oil = OilDefinition.builder()
    .yRange(-48, 32)
    .minSolidCover(2)
    .build();
```

Query the active profile and sample exact production geology on the server:

```java
MineralogyApi.getActiveProfile(server).ifPresent(profile ->
    LOGGER.info("Configured rocks: {}", profile.rockIds().size()));

MineralogyApi.createSampler(server.overworld()).ifPresent(sampler -> {
    GeologyColumn column = sampler.sampleColumn(120, -40, 92);
    LOGGER.info("{} / {} / {}", column.biome(), column.geome(), column.rockAt(20));
});
```

`sampleColumn` performs one biome/geome classification and reuses it for every
Y query. Sampling is read-only and is intended for gameplay decisions,
diagnostics, and compatible generation outside Mineralogy's block loops.
Callbacks inside Mineralogy generation loops are intentionally unsupported.

`MineralogyOreIntegration` remains as a deprecated facade for 5.x consumers.
