# Mineralogy Ore Provider Integration

Mods can delegate ore placement to Mineralogy without taking a compile-time
dependency. The provider mod remains responsible for registering its blocks,
items, loot, recipes, textures, and tags.

## Provider File

Create `config/<modid>-mineralogy.json` before Forge's load-complete phase. The
file name prefix and `provider_modid` must match the installed provider mod.

```json
{
  "schema_version": 1,
  "provider_modid": "basemetals",
  "provider_revision": 1,
  "ores": {
    "basemetals:copper_ore": {
      "dimensions": {
        "minecraft:overworld": {
          "enabled": true,
          "min_y": -16,
          "max_y": 96,
          "frequency": 8.0,
          "quantity": 8,
          "host_families": [
            "sedimentary",
            "metamorphic",
            "igneous_intrusive"
          ],
          "geomes": {
            "mountain_belt": 1.4
          }
        },
        "minecraft:the_nether": {
          "enabled": true,
          "min_y": 8,
          "max_y": 120,
          "frequency": 3.0,
          "quantity": 6,
          "host_tags": ["minecraft:base_stone_nether"],
          "host_blocks": ["minecraft:blackstone"]
        }
      }
    }
  }
}
```

`frequency` is expected attempts per chunk. Its whole part is guaranteed and
its fractional part is the probability of one additional attempt. Host block
and tag lists are additive. Overworld entries may additionally use Mineralogy
rock families and geome multipliers. Other dimensions use block and tag hosts.

The provider file is accepted atomically. A bad schema, unknown ore block,
invalid enabled dimension, or ownership collision rejects the whole provider.
Mineralogy logs one diagnostic and the provider must retain native generation.

## Takeover API

Create the provider file during common setup. Query
`com.mcmoddev.mineralogy.api.MineralogyOreIntegration` during IMC processing or
later, before adding native biome features. Mineralogy performs its definitive
scan during IMC enqueue, after every mod's common setup has completed:

```java
if (!MineralogyOreIntegration.isProviderActive("basemetals")) {
    addNativeOreGeneration();
}
```

`getProviderStatus` exposes `PENDING`, `ACTIVE`, and `INACTIVE` when a provider
needs to distinguish lifecycle timing. Suppress native generation only for
`ACTIVE`.

Provider defaults are copied into each new world's
`serverconfig/mineralogy-geology.json`. Existing worlds import only newly added
ore IDs from later provider revisions; established world values are never
overwritten. Provider ores removed upstream remain snapshotted and are marked
orphaned for backward compatibility.
