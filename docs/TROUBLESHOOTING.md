# Troubleshooting

## Provider remains inactive

Check that the provider mod is loaded, the file name and `provider_modid`
match, `provider_revision` is positive, all provider-owned IDs use its
namespace, all referenced blocks exist, and every enabled ore or terrain
dimension has hosts. A malformed provider file deliberately prevents fallback
to API declarations.

## Custom terrain does not appear

Confirm the dimension is enabled in `terrain_dimensions`, its host blocks or
tags resolve, at least one enabled rock includes that dimension ID, and its
biome restrictions match the dimension's actual biomes.

## Changes do not affect existing terrain

Mineralogy never rewrites generated chunks. Travel to new chunks or create a
new test world. Restart after editing JSON or changing API declarations.

## Server differs from the client test world

Copy the world's `serverconfig/mineralogy-geology.json`, not merely the global
client config. Install the same provider mods and blocks on the server.

## Native ores duplicate

Provider mods must suppress native generation only after
`MineralogyApi.isOreTakeoverActive(modid)` becomes true. Keep native generation
for `PENDING` and `INACTIVE`.
