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
- `forge/{version}/` - Forge platform implementation (1.20.x)
- `fabric/base/`, `neoforge/base/`, `forge/base/` - Base platform code (included as srcDirs)
- `props/` - Version-specific properties files

Supported versions:
- 1.21.1: Fabric, NeoForge
- 1.20.1: Fabric, Forge

## Build Commands

```bash
./gradlew build                                    # Build default version (1.21.1)
./gradlew build -Ptarget_mc_version=1.20.1         # Build specific version
./gradlew fabric:runClient                         # Run Fabric client
./gradlew neoforge:runClient                       # Run NeoForge client
./gradlew forge:runClient -Ptarget_mc_version=1.20.1  # Run Forge client
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

### Version-specific API differences (1.21.1 vs 1.20.1)

- SavedData: 1.21 uses `HolderLookup.Provider` parameter, 1.20.1 does not
- ResourceLocation: 1.21 uses `fromNamespaceAndPath()`, 1.20.1 uses constructor
- Codec: 1.21 uses `MapCodec`, 1.20.1 uses `Codec`

### Forge 1.20.1 with Architectury Loom

`loom.platform = forge` must be set in `forge/{version}/gradle.properties` (Loom defaults to Fabric mode).

## Scripts

- `scripts/convert_nbt_1_21_to_1_20.py` - Convert structure NBT files from 1.21.1 to 1.20.1 format
- `scripts/release.sh` - Upload a single JAR to Modrinth
- `scripts/release-all.sh` - Upload all JARs in `build/release/` to Modrinth
