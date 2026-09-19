# Castle Shift: Runtime Generation Config Design

## Purpose

`docs/future-expansion-plan.md` ("Generation Frequency and Compatibility")
proposes publishing example datapacks (sparse/default/frequent) as a
stop-gap, and notes that "a future in-game configuration option is useful
only if it can be maintained consistently across all supported loaders and
versions." This spec defines that configuration option: a config file plus
an in-game settings screen that let a player or server operator enable or
disable castle generation and control its frequency, without requiring a
world restart to pick a different datapack.

This follows the same no-external-dependency pattern already proven in the
sibling MinersMarket project (vanilla widgets only; no Cloth Config, no
mandatory ModMenu).

## Scope

- In scope: one config-controlled structure set, `castleshift:castle`
  (today's only castle). The data model is written so that adding a second
  structure_set entry later (e.g. a future large-castle archetype) is a
  data addition, not a redesign.
- Out of scope: the large-castle archetype itself, castle states, and loot
  changes described elsewhere in `future-expansion-plan.md`. Those remain
  future work.
- Out of scope: per-structure_set UI/config keys beyond `castle`. Only one
  entry is implemented now (YAGNI).

## Current Baseline

- `common/1.21.1/.../worldgen/structure_set/castle.json` uses
  `minecraft:random_spread` with fixed `spacing: 32`, `separation: 8`,
  `salt`, and a village `exclusion_zone`. This file is copied/converted into
  every other supported version during the build.
- No config file, config screen, or settings infrastructure exists in the
  project today.
- `night-config` (the TOML library MinersMarket uses) is already present as
  a transitive dependency of NeoForge/Forge (confirmed under
  `run-prod/cache/*/libraries/com/electronwill/night-config`).

## Data Model

```
Generation {
  enabled: boolean = true
  preset: SPARSE | DEFAULT | FREQUENT = DEFAULT
  customSpacing: Integer? = null    // null => use preset's spacing
  customSeparation: Integer? = null // null => use preset's separation
}
```

- `customSpacing`/`customSeparation` are independent: either, both, or
  neither may be set. A non-null value always wins over the preset's value
  for that field, per the user's explicit request for "preset with numeric
  override, numeric wins when present."
- Preset spacing/separation values live in `ConfigDefaults` as named
  constants. Starting point (tune during implementation if needed):
  - `SPARSE`: spacing 64, separation 16
  - `DEFAULT`: spacing 32, separation 8 (matches today's shipped value)
  - `FREQUENT`: spacing 16, separation 4
- Validation (`ConfigRanges`, shared by loader and screen, same pattern as
  MinersMarket): `separation < spacing` must hold for the effective
  (post-override) values. Spacing/separation each have a sane min/max
  (e.g. spacing 8-512, separation 1-256) to keep worldgen cost bounded.
- Config is server-authoritative (worldgen only ever runs server-side).
  Screen shows MinersMarket's existing warning banner when
  `minecraft.level != null && !minecraft.hasSingleplayerServer()`, since
  edits then only affect the local player's view of a remote server, not
  the actual host.

## Storage Layout

Mirrors MinersMarket's `common/{version}/.../config/` package, duplicated
per version directory because it depends on version-specific Minecraft
APIs (`Screen`, `CycleButton`, etc.) the same way `StructureMaterialHelper`
and its processors already are:

```
common/{version}/src/main/java/com/castleshift/config/
├── CastleShiftConfig.java   # immutable record + static get/set holder
├── ConfigDefaults.java      # preset constants + defaults()
├── ConfigRanges.java        # min/max + separation<spacing validation
├── ConfigLoader.java        # night-config TOML read, clamp/fallback, schema_version
├── ConfigWriter.java        # write-back preserving comments (CommentedFileConfig)
└── client/
    └── ConfigScreen.java    # vanilla-widget settings screen
```

Bundled default `castleshift.toml` resource ships with commented
sections, following MinersMarket's convention.

## Worldgen Integration

### Mechanism

Replace `minecraft:random_spread` in `castle.json`'s `placement.type` with
a new custom type, `castleshift:configurable_spread`. This type:

- Extends the same base contract as `RandomSpreadStructurePlacement`
  (`StructurePlacement`, exposing `isPlacementChunk`), so it still inherits
  vanilla's `locate_offset`, `frequency`, `frequency_reduction_method`,
  `salt`, and `exclusion_zone` handling from the base codec — only
  `spacing`/`separation` (and a new `enabled` escape hatch) are sourced
  from config instead of the JSON.
- Reimplements the same spacing-grid distribution algorithm vanilla's
  `RandomSpreadStructurePlacement` uses, reading spacing/separation from
  `CastleShiftConfig` at call time (not cached at load), so a config change
  takes effect for the next chunk generated without requiring a world
  reload. Already-generated chunks are unaffected — identical to changing
  spacing in a datapack today.
- Returns no placement chunk immediately when `enabled=false`.

### Registration

Follows the existing `StructureProcessor` registration pattern documented
in `CLAUDE.md` (`world/processor/`), extended to `world/placement/`:

- Fabric: register directly in `onInitialize`.
- NeoForge/Forge: `DeferredRegister` on the mod event bus (not direct
  `Registry.register`, which fails on the frozen registry).
- 26.2 uses the interface-based `StructurePlacementType` split
  (`MapCodec<? extends StructurePlacement>` in the registry) the same way
  `StructureProcessor` already branches for 26.2 vs 26.1.2/1.21.x.

### JSON changes

`castle.json`'s `placement` block drops `spacing`/`separation` (now
config-driven) but keeps `salt` and `exclusion_zone`:

```json
"placement": {
  "type": "castleshift:configurable_spread",
  "salt": 194572831,
  "exclusion_zone": { "other_set": "minecraft:villages", "chunk_count": 10 }
}
```

## Config Screen

Directly follows MinersMarket's `ConfigScreen` (`common/{ver}/.../config/client/ConfigScreen.java`, `ContainerObjectSelectionList`-based):

- `ToggleEntry` for `enabled` (on/off).
- `CycleButton`-based entry for `preset` (SPARSE/DEFAULT/FREQUENT).
- Two `NumberEntry` rows for `customSpacing`/`customSeparation`, blank =
  null (use preset). Live validation via `EditBox.setResponder`; invalid
  or `separation >= spacing` (after resolving overrides) turns the field
  red and disables Done, same as MinersMarket's cross-field check.
- Footer: Reset (to `ConfigDefaults.defaults()`) / Cancel / Done (writes
  via `ConfigWriter`).
- `resize()` snapshots unsaved field values before `init()` rebuilds
  widgets.

### Entry points

- NeoForge: `IConfigScreenFactory` extension point.
- Forge (both the ForgeGradle 1.21.x/26.x path and the Loom 1.20.1 path):
  `ConfigScreenHandler.ConfigScreenFactory`, keeping the existing
  Forge-47-vs-56 event-bus branching used elsewhere in the mod.
- Fabric: a new optional `fabric/base-modmenu` module (ModMenu
  `compileOnly`, source-set-included only when present) implementing
  `ModMenuApi`, mirroring MinersMarket's `ModMenuIntegration`.
- All loaders: an unbound `KeyMapping` ("Open Castle Shift Config") checked
  on the client tick, per the confirmed requirement to keep this consistent
  with MinersMarket. Lives in a nested client-only holder class so it is
  never class-loaded on a dedicated server.

## docs/future-expansion-plan.md Update

Amend "Generation Frequency and Compatibility" to state:

- Example datapacks remain useful as an immediate, no-code-change option
  and as a fallback for players who prefer editing a datapack directly.
- The permanent solution is a bundled config file (TOML) plus an in-game
  settings screen, following the vanilla-widget pattern already proven in
  MinersMarket (no Cloth Config / ModMenu dependency required).
- Config controls enable/disable and a spacing/separation preset
  (sparse/default/frequent) with optional numeric overrides that take
  priority over the preset when set.

## Error Handling

- `ConfigLoader` clamps out-of-range values to the nearest valid bound and
  falls back to defaults for unparseable fields, logging a warning (same
  as MinersMarket) rather than crashing world load.
- If `customSpacing`/`customSeparation` combine with the preset to violate
  `separation < spacing`, the loader clamps `customSeparation` down to
  `customSpacing - 1` rather than rejecting the whole config, so a bad
  manual edit to the TOML file can't silently disable worldgen data
  loading.
- `schema_version` mismatch (older config from a prior mod version) logs
  a warning and fills missing fields from defaults; no hard failure.

## Testing

- `ConfigLoaderTest`: defaults, clamping, schema_version fallback, TOML
  round-trip via `ConfigWriter`.
- `ConfigurableSpreadStructurePlacementTest` (or equivalent, following
  `common/{version}/src/test/.../ProcessorTestInvoker.java`'s pattern for
  absorbing the 26.2 signature differences): `enabled=false` yields no
  placement chunk; preset values apply when no override is set; a custom
  override takes priority over the active preset; one-sided overrides
  (only spacing or only separation set) resolve correctly.
- Manual verification (per `future-expansion-plan.md`'s existing
  Verification Requirements): with `enabled=false`, confirm no new castles
  generate in freshly explored chunks; with FREQUENT vs SPARSE, confirm
  the spacing difference is observable; confirm existing chunks/already
  generated castles are unaffected by a config change. Run on at least
  1.21.1 (class-based StructureProcessor API) and one 26.2+ version
  (interface-based split) to cover both registration branches.

## Open Questions / Follow-ups (non-blocking)

- Exact preset spacing/separation numbers are a starting point and may be
  tuned during implementation based on manual playtesting.
- Extending the config to a second structure_set (e.g. a future
  large-castle archetype) is intentionally deferred; the record/JSON
  design should not need restructuring when that happens, only an
  additional named entry.
