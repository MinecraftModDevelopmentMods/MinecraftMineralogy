# Migration

Mineralogy 6.0 reads global schemas 1-3, world schemas 1-2, and ore-provider
schema 1. Migrated files preserve established geology and receive only missing
6.0 sections. Backups are created before global configuration replacement.

World schema 2 already contained a complete snapshot, so migration preserves
all formations, rock edits, geomes, biome rules, ores, oil, aliases, and
provider tombstones. World schema 1 is overlaid on the currently effective
installed-pack profile because it contained only the earlier small selection.

Existing worlds import only provider IDs not seen before. Existing edits are
never overwritten. Disabled or removed entries remain tombstones. Entries from
a provider that disappears remain in the snapshot and are marked orphaned.
Templates never migrate or rewrite an existing profile.

Legacy unqualified built-in geome names continue to work and are exposed by
the public API as `mineralogy:<name>`.
