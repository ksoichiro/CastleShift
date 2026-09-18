# CastleShift

A structure mod for Minecraft that adds castles to the world. Multi-loader, multi-version mod using Architectury.

- Mod ID: `castleshift`
- Package: `com.castleshift`
- Main class: `CastleShift`
- Gradle project names use `-` separator (e.g. `:common-1.21.1`, `:fabric`), but directories use `/` (e.g. `common/1.21.1/`, `fabric/1.21.1/`)

## Project Structure

- `common/shared/` - Version-agnostic shared code
- `common/{version}/` - Version-specific common code
- `fabric/{version}/` - Fabric platform implementation
- `neoforge/{version}/` - NeoForge platform implementation (1.21+)
- `forge/{version}/` - Forge platform implementation
- `fabric/base/`, `neoforge/base/`, `forge/base/` - Base platform code (included as srcDirs)
- `forge/base-56/` - Base platform code for Forge 56+ (MC 1.21.6+, uses new EventBus API)
- `props/` - Version-specific properties files

Supported versions:
- 26.1.2, 26.2: Fabric, NeoForge, Forge (Minecraft's year-based versioning; requires JDK 25)
- 26.3: Fabric, NeoForge (Forge not yet released upstream for this version)
- 1.21.1〜1.21.11: Fabric, NeoForge, Forge (except 1.21.2 which has no Forge release)
- 1.20.1: Fabric, Forge

## Build Commands

```bash
./gradlew build                                    # Build default version (1.21.1)
./gradlew build -Ptarget_mc_version=1.20.1         # Build specific version
./gradlew fabric:runClient                         # Run Fabric client
./gradlew neoforge:runClient                       # Run NeoForge client
./gradlew forge:runClient                          # Run Forge client (1.21.x, ForgeGradle)
./gradlew forge:runClient -Ptarget_mc_version=1.20.1  # Run Forge client (1.20.1, Loom)
./gradlew buildAll                                 # Build all versions
./gradlew release                                  # Clean + build all + collect JARs
```

## Key Files

- `gradle.properties` - Mod version, target MC version
- `props/{version}.properties` - Version-specific dependencies
- `common/shared/src/main/java/com/castleshift/CastleShift.java` - Main mod class

## Resources Location

- Assets: `common/{version}/src/main/resources/assets/castleshift/`
- Data: `common/{version}/src/main/resources/data/castleshift/`
- Structure files: `common/{version}/src/main/resources/data/castleshift/structure/`
- Worldgen config: `common/{version}/src/main/resources/data/castleshift/worldgen/`
- Mixin config: `common/{version}/src/main/resources/castleshift.mixins.json`

## Development Notes

- Use Architectury API for cross-loader compatibility
- Platform-specific code goes in `fabric/*/`, `neoforge/*/`, or `forge/*/`
- Version-specific common code goes in `common/{version}/`

### Known issue: `neoforge:runClient -Ptarget_mc_version=1.21.1` may fail to launch

`neoforge:runClient` for 1.21.1 can crash at startup with
`InvalidModFileException: Illegal version number specified version (main)`
(NeoForge fails to associate `neoforge.mods.toml` with the mod, then falls
back to the `build/resources/main` directory's own basename as a pseudo
version). Extensively investigated (2026-09-18): reproduces even from a
fully clean environment (wiped `.gradle`, wiped and redownloaded the
`fabric-loom` 1.21.1/NeoForge caches), on commits that predate any
buildSrc/NBT-conversion work, so it is not caused by this project's build
scripts. Root cause not found; no matching upstream architectury-loom issue
located either. Does not affect `build`/`buildAll`/`release` (verified
repeatedly) — only the interactive dev-run convenience task. `forge:runClient`
and `neoforge:runClient -Ptarget_mc_version=1.21.2` (and later) are unaffected.
Workaround: test 1.21.1 behavior via `forge:runClient` or a 1.21.2+ target
instead of `neoforge:runClient -Ptarget_mc_version=1.21.1`.

### Version-specific API differences (1.21.1 vs 1.20.1)

- SavedData: 1.21 uses `HolderLookup.Provider` parameter, 1.20.1 does not
- ResourceLocation: 1.21 uses `fromNamespaceAndPath()`, 1.20.1 uses constructor
- Codec: 1.21 uses `MapCodec`, 1.20.1 uses `Codec`

### Minecraft 26.x notes (26.1.2, 26.2, 26.3)

- **Toolchain**: requires JDK 25, Gradle 9, and Architectury Loom 1.17 with the `loom-no-remap` plugin (26.x ships non-obfuscated, so `use_mojang_mappings=false` in props routes to `loom-no-remap`; 1.20.1/1.21.x keep official Mojang mappings). Forge uses ForgeGradle 7 (see Forge Build System).
- **StructureProcessor API split**: 26.2 made `StructureProcessor` an interface (`codec()`, `processBlock(..., BlockPos offset, blockInfo, ...)`), and `BuiltInRegistries.STRUCTURE_PROCESSOR` holds `MapCodec<? extends StructureProcessor>`. 26.1.2 still uses the class-based form (`extends StructureProcessor`, `getType()`, two-info `processBlock`, `StructureProcessorType<?>` registry) like 1.21.x.
- **Processor registration**: register on NeoForge/Forge via `DeferredRegister` on the mod event bus (NOT direct `Registry.register` in the constructor, which fails on the frozen registry). Fabric registers directly in `onInitialize` via `ModProcessors.init()`. For 26.2 the DeferredRegister element type is `MapCodec<? extends StructureProcessor>`.
- **26.x Forge** omits the `forge/base-56` srcDir and ships a version-specific entrypoint (base-56 uses the removed `StructureProcessorType` API).
- **pack.mcmeta** uses the array `pack_format` form (`min_format`/`max_format`, e.g. `[94, 1]`).
- **Shared tests** call `processBlock` through the per-version `common/{version}/src/test/.../ProcessorTestInvoker.java` to absorb the 26.2 signature change.

### Forge Build System

- **Forge 1.21.x and 26.x** (Forge 52+): Uses **ForgeGradle** directly (`net.minecraftforge.gradle` plugin). Architectury Loom's `loom.platform = forge` has JPMS split package conflicts on 1.21+. ForgeGradle 7 (`[7.0.17,8.0)`) is required for Gradle 9 / 26.x; ForgeGradle 6 rejects Gradle 9.
- **Forge 1.20.1** (Forge 47): Uses **Architectury Loom** with `loom.platform = forge` in `forge/1.20.1/gradle.properties`.
- `forge_major_version` in `props/{version}.properties` controls ForgeGradle-specific conditional logic (reobf, JPMS, EventBus).

### Forge API differences by version

- Forge 47〜55 (MC 1.20.1〜1.21.5): `FMLJavaModLoadingContext.get().getModEventBus()` for event bus access
- Forge 56+ (MC 1.21.6+): Constructor injection of `FMLJavaModLoadingContext`, use `context.getModBusGroup()` for registration (EventBus 7 migration)

## Scripts

- `gradle/nbt-conversion.gradle` - Converts structure NBT files and copies JSON data files from 1.21.1 to 1.20.1 format, applied from `common/1.20.1/build.gradle`
- `scripts/release.sh` - Upload a single JAR to Modrinth
- `scripts/release-all.sh` - Upload all JARs in `build/release/` to Modrinth
- `scripts/release-curseforge.sh` - Upload a single JAR to CurseForge
- `scripts/release-curseforge-all.sh` - Upload all JARs in `build/release/` to CurseForge
