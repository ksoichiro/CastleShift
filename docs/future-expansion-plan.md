# Castle Shift: Future Expansion Plan

## Purpose

Player feedback points to a shared need: castles should be places to explore, not only landmarks to discover. The next expansions should add meaningful occupants, rewards, and room to explore while preserving the mod's lightweight, datapack-friendly foundation.

This document records a proposed direction. It is not a release commitment.

## Current Baseline

Castle Shift currently generates four-floor, jigsaw-based castles with randomized room templates and material variations. The canonical world-generation data lives under `common/1.21.1` and is copied into later supported versions during the build.

- The castle structure uses jigsaw generation with `size: 7` and `max_distance_from_center: 100`.
- Castle placement uses random spread with `spacing: 32`, `separation: 8`, and a 10-chunk exclusion zone around villages.
- The structure declares no spawn overrides.
- A basic barrel loot table exists, with iron swords, diamonds, and bread.
- Structure placement, biome filters, and loot tables can already be overridden by a datapack.

The reported concerns are therefore less about the absence of every system and more about how little those systems currently shape the exploration experience.

## Design Goals

- Make each castle feel like a place with a history and a purpose.
- Tie risk, occupants, layout, and rewards together.
- Give modpack authors control over loot and generation frequency.
- Keep other-mod integrations optional and avoid hard dependencies.
- Protect world-generation stability across supported loaders and Minecraft versions.

## Castle States

Rather than putting the same mobs and loot in every castle, introduce themed castle states. A state should determine its room templates, residents, encounter difficulty, and loot tables.

| State | Theme and occupants | Exploration and rewards |
| --- | --- | --- |
| Abandoned castle | Undead, spiders, damaged rooms, overgrown or ruined details | Modest supplies, hidden rooms, and occasional valuables |
| Pillager keep | Pillagers, vindicators, and a captain in selected layouts | Armory and treasury loot balanced around combat risk |
| Settled castle | Villagers, an iron golem, workspaces, storage, and stables | Trading, a safe landmark, and modest household supplies |
| Ruined stronghold | Stronger encounters, traps, collapsed rooms, and restricted routes | A rare final reward behind a meaningful challenge |

This provides a clear answer to both common requests: add monsters and loot, but make them belong to the castle rather than placing generic content into every room.

Settled castles need extra care. Beds, workstations, village proximity, raids, and iron golem behavior can create gameplay side effects. They should follow the hostile variants only after their behavior and placement rules have been tested.

## Larger, More Explorable Castles

Increasing the jigsaw `size` alone is not a reliable way to make castles larger. It changes generation depth, not the physical scale or usefulness of the available templates. A larger castle needs compatible room pieces, reliable paths between floors, and an outer layout that supports its footprint.

The preferred approach is to add a separate large-castle archetype, selected alongside the existing castle through weighted structure generation. Its template set can include:

- a central hall and courtyard;
- barracks, kitchens, storerooms, and a chapel or audience room;
- prison or cellar sections beneath the main floor;
- towers with purposeful vertical routes;
- optional side wings and secret rooms.

Keeping the current castle as a normal-sized archetype avoids making every generated castle expensive or overwhelming. Large castles can be rarer and can carry the more demanding encounters and rewards.

## Loot and Modpack Integration

Loot should first be split by purpose instead of growing one shared barrel table. Suggested tables include `common`, `larder`, `armory`, `treasury`, and state-specific tables such as `pillager` or `abandoned`.

The base mod should contain only vanilla items and no mandatory integrations. Modpack support should be provided through datapacks that replace or extend these tables. This lets a pack add items from its own mods, tune progression, or remove unwanted rewards without requiring Castle Shift to know about every possible dependency.

Optional built-in integrations can be considered later for widely used loot systems when there is clear demand and a stable API. Each integration should remain optional and must fall back cleanly when the target mod is absent.

## Generation Frequency and Compatibility

Some players may find castles too common, especially in compatible overworld biomes. The default placement currently uses 32-chunk spacing, subject to biome eligibility and the village exclusion zone.

Before changing the default, publish example datapacks for sparse, default, and frequent generation. This gives players an immediate answer without forcing one world-generation preference on every pack. A future in-game configuration option is useful only if it can be maintained consistently across all supported loaders and versions.

## Delivery Order

1. Split loot into purpose-based tables and add the first themed room templates.
2. Release abandoned castles and pillager keeps with state-appropriate encounters and rewards.
3. Add the large-castle archetype with a larger internal room network.
4. Add settled castles after validating villager, golem, raid, and village interactions.
5. Evaluate optional integrations based on modpack and player demand.

## Verification Requirements

Every world-generation expansion should be checked for:

- placement on plains, forests, slopes, snow, and uneven terrain;
- inaccessible rooms, broken jigsaw joins, and missing vertical routes;
- collisions or awkward proximity with villages and other structures;
- loot balance and container placement for every castle state;
- structure-specific spawning behavior and performance under normal exploration;
- consistency on every supported Minecraft version and loader.

Large castles and new resident types should be tested in a modded world as well as a minimal installation. World-generation bugs are difficult to repair after chunks have been generated, so stability takes priority over adding more variants quickly.
