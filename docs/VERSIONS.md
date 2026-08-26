# Mineralogy Versioning

Mineralogy releases use a four-component version so the feature set and the
exact Minecraft/loader target are both visible in one number.

## Version format

```text
Major.Minor.Bug.Target
```

The first three components are the **functional version**. Mineralogy `6.0.1`
means major generation 6, minor release 0, and bug revision 1.

The fourth component identifies the target build. The complete version for
this Minecraft 1.15.2 Forge release is therefore:

```text
6.0.1.115021
```

This expanded numeric form is compatible with Maven version ordering, but it
is not strict Semantic Versioning 2.0 because SemVer defines three numeric core
components.

The release tag is exactly the complete four-component version, with no
redundant Minecraft-version prefix. For this branch the tag is therefore
`6.0.1.115021`, not `1.15.2-6.0.1.115021`. The Target already makes tags unique
across Minecraft versions and loaders.

## Reading the target component

The target is deterministic; it is not a CI run number or another feature
revision.

To calculate it:

1. Write the Minecraft version as `major.minor.patch`, using zero when the
   patch component is omitted.
2. Concatenate the Minecraft major number without padding, the minor number as
   two digits, the patch number as two digits, and the one-digit loader code.
3. Use loader code `1` for Forge and `2` for NeoForge.

It can be decoded from right to left: one loader digit, two patch digits, two
minor digits, and all remaining digits for the Minecraft major version.

| Minecraft | Loader | Target | Example complete version |
| --- | --- | ---: | --- |
| 1.10.2 | Forge | `110021` | `6.0.1.110021` |
| 1.12.2 | Forge | `112021` | `6.0.1.112021` |
| 1.14.4 | Forge | `114041` | `6.0.1.114041` |
| 1.15.2 | Forge | `115021` | `6.0.1.115021` |
| 1.18.2 | Forge | `118021` | `6.0.0.118021` |
| 1.20.6 | Forge | `120061` | `6.0.0.120061` |
| 1.21.11 | Forge | `121111` | `6.0.0.121111` |
| 26.2 | Forge | `2602001` | `6.0.0.2602001` |
| 26.2 | NeoForge | `2602002` | `6.0.0.2602002` |

The 1.15.2 row records this branch's current release. The other rows illustrate
target encoding only; they do not claim that the 6.0.1 changes have already
been forward-ported.

Historical Mineralogy releases may also have four numeric components that used
the last number as an ordinary build sequence. The target policy applies to
new releases and does not reinterpret an old file's version.

## Major version

The **Major** number changes for a fundamental or breaking new generation of
Mineralogy.

Mineralogy 6 is the clearest example: Mineralogy now provides rocks, ores,
fluids, recipes, and an OreSpawn provider, while OreSpawn exclusively owns
terrain, strata, ore placement, deposits, profiles, and their user interface.
That is a larger architectural change than an ordinary feature or fix.

Compatibility adaptations needed for another Minecraft or loader version do
not by themselves require a major increase when players and integrations
receive the same supported behaviour.

## Minor version

The **Minor** number changes for a substantial feature or behavioural change
that does not justify a new major generation. Examples include a significant
new family of player-usable content or a major overhaul of an existing system.

When Major changes, Minor and Bug reset to zero. When Minor changes, Bug resets
to zero. The target for the actual build is then appended:

```text
6.0.1.115021 -> 6.1.0.115021
6.1.4.115021 -> 7.0.0.115021
```

## Bug version

The **Bug** number changes for a bug fix or very small feature. Examples
include fixing saved inventory loading, correcting a recipe or translation,
or making a small configuration or documentation improvement that warrants a
release.

Other projects often call this the patch number. MMD uses **Bug** to describe
its purpose and **Target** for the Minecraft/loader qualifier.

## Ports to other Minecraft versions

A functionally equivalent port keeps the same `Major.Minor.Bug` version and
changes only its Target. For example, equivalent Mineralogy 6 releases could
be:

```text
Minecraft 1.10.2 / Forge / Mineralogy 6.0.0.110021
Minecraft 1.12.2 / Forge / Mineralogy 6.0.1.112021
Minecraft 1.14.4 / Forge / Mineralogy 6.0.1.114041
Minecraft 1.15.2 / Forge / Mineralogy 6.0.1.115021
Minecraft 1.18.2 / Forge / Mineralogy 6.0.0.118021
```

Minecraft and loader APIs may require different internal code without changing
the functional version. If a port also adds a player-visible feature or fix,
the Major, Minor, or Bug component must be assessed separately; the Target
still identifies the build's actual platform.

## Branch-specific fixes and skipped numbers

Functional versions are allocated across Mineralogy as a whole. One functional
number must not describe unrelated changes on different Minecraft branches.
The same number may be shared by branches only when the releases are intended
to provide the same functional change.

If a fix applies only to one old branch, that branch may receive the next Bug
number while other branches remain unchanged. A later, unrelated fix on
another branch uses the next unused number even if that branch never needed
the earlier fix. Skipped Bug numbers are therefore valid.

This means a higher version on another Minecraft branch does not necessarily
contain every lower-numbered platform-specific fix. Release notes remain the
authoritative description of included changes.

## Dependencies and modpacks

Dependency ranges normally describe compatible functional releases. For
example, `[6.0.0,7.0.0)` accepts target-qualified Mineralogy 6 releases while
excluding Mineralogy 7. Minecraft and loader metadata still decide whether a
particular jar can load on the current game.

Mineralogy 6 also requires OreSpawn `[4.0.6,5.0.0)`. OreSpawn uses the same
target calculation, so the matching Minecraft 1.15.2 Forge release is
`4.0.9.115021`. The dependency range deliberately describes the supported
functional OreSpawn generation; Forge still prevents jars for another
Minecraft target from loading together.

Pack maintainers should record the complete four-component versions in pack
manifests and support reports. Saying only “Mineralogy 6” identifies the
feature generation, not the exact jar.

## Builds and release notes

The Gradle build reads the complete version from `mod_version`, verifies that
it has four numeric components, and checks that its Target matches the declared
Minecraft version and Forge loader. CI build numbers are not appended. For this
branch, published metadata and artifacts therefore use `6.0.1.115021`.

Every release note should state:

- the Minecraft version and loader;
- the complete four-component version;
- the functional `Major.Minor.Bug` version;
- the features and fixes actually included;
- relevant migration, configuration, and compatibility information.

The goal is a version that is useful to players, pack authors, mod developers,
release tooling, and support teams without pretending that all maintained
Minecraft branches contain identical implementation details.
