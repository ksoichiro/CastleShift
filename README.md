# Castle Shift

**A structure mod that adds castles to Minecraft's world generation**

A multi-loader Minecraft mod that generates multi-story stone brick castles across the landscape. Each castle features randomized structural variations, so no two castles are exactly alike.

![Castle Shift Overview](docs/screenshots/01.png)

*More screenshots available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/castle-shift) and [Modrinth](https://modrinth.com/mod/castle-shift)*

## Features

- **Multi-Story Castles**: Grand 4-floor castles with walls, towers, and corridors
- **Randomized Materials**: Roofs, walls, and stairs are each built from materials randomly selected per castle, producing a wide range of appearances
  - **Roofs**: Spruce, Dark Oak, Deepslate Bricks, Dark Prismarine, Red Nether Bricks, Crimson
  - **Walls**: Stone Bricks, Deepslate Bricks, Polished Blackstone Bricks, Sandstone, End Stone Bricks
  - **Stairs**: Bricks, Stone Bricks, Polished Granite, Deepslate Bricks, Polished Blackstone, Polished Blackstone Bricks, Red Nether Bricks
- **Structural Variations**: Each floor section (walls, corners, centers, towers) has multiple structure templates chosen at random
- **Wall Weathering**: Stone brick and polished blackstone brick walls may randomly degrade into cobblestone, andesite, or cracked variants for a lived-in look
- **Natural Placement**: Castles generate organically in a wide variety of biomes
- **Datapack-Friendly**: Structure placement can be customized via datapacks (spacing, separation, biome filters)

## Supported Versions

| Minecraft | Mod Loader |
|-----------|-----------|
| 1.21.1 | Fabric Loader 0.17.3+ with Fabric API 0.116.7+1.21.1 |
| 1.21.1 | NeoForge 21.1.209+ |
| 1.21.1 | Forge 52.1.0+ |
| 1.20.1 | Fabric Loader 0.16.10+ with Fabric API 0.92.2+1.20.1 |
| 1.20.1 | Forge 47.3.0+ |

## Requirements

### For Players
- **Minecraft**: Java Edition 1.21.1 or 1.20.1
- **Mod Loader** (choose one for your Minecraft version):
  - **1.21.1**: Fabric Loader 0.17.3+ with Fabric API 0.116.7+1.21.1, OR NeoForge 21.1.209+, OR Forge 52.1.0+
  - **1.20.1**: Fabric Loader 0.16.10+ with Fabric API 0.92.2+1.20.1, OR Forge 47.3.0+

### For Developers
- **Java Development Kit (JDK)**: 21 or higher
- **IDE**: IntelliJ IDEA (recommended) or Eclipse

## Building from Source

```bash
git clone https://github.com/ksoichiro/CastleShift.git
cd CastleShift
./gradlew build
```

**Build for a specific version**:
```bash
./gradlew build -Ptarget_mc_version=1.20.1
```

**Output Files** (1.21.1):
- `fabric/1.21.1/build/libs/castleshift-0.1.0+1.21.1-fabric.jar` - Fabric loader JAR
- `neoforge/1.21.1/build/libs/castleshift-0.1.0+1.21.1-neoforge.jar` - NeoForge loader JAR
- `forge/1.21.1/build/libs/castleshift-0.1.0+1.21.1-forge.jar` - Forge loader JAR

**Output Files** (1.20.1):
- `fabric/1.20.1/build/libs/castleshift-0.1.0+1.20.1-fabric.jar` - Fabric loader JAR
- `forge/1.20.1/build/libs/castleshift-0.1.0+1.20.1-forge.jar` - Forge loader JAR

**Multi-version tasks**:
```bash
./gradlew buildAll       # Build all versions
./gradlew cleanAll       # Clean all versions
./gradlew release        # cleanAll + buildAll + collectJars
```

## Development Setup

### Import to IDE

#### IntelliJ IDEA (Recommended)
1. Open IntelliJ IDEA
2. File -> Open -> Select `build.gradle` in project root
3. Choose "Open as Project"
4. Wait for Gradle sync to complete

### Run in Development Environment

```bash
# Fabric client (1.21.1)
./gradlew fabric:runClient

# NeoForge client (1.21.1)
./gradlew neoforge:runClient

# Forge client (1.21.1)
./gradlew forge:runClient

# Fabric client (1.20.1)
./gradlew fabric:runClient -Ptarget_mc_version=1.20.1

# Forge client (1.20.1)
./gradlew forge:runClient -Ptarget_mc_version=1.20.1
```

## Installation

### For Minecraft 1.21.1

#### Fabric
1. Install Minecraft 1.21.1
2. Install Fabric Loader 0.17.3+
3. Download and install Fabric API 0.116.7+1.21.1
4. Copy the Fabric JAR to `.minecraft/mods/` folder
5. Launch Minecraft with Fabric profile

#### NeoForge
1. Install Minecraft 1.21.1
2. Install NeoForge 21.1.209+
3. Copy the NeoForge JAR to `.minecraft/mods/` folder
4. Launch Minecraft with NeoForge profile

#### Forge
1. Install Minecraft 1.21.1
2. Install Forge 52.1.0+
3. Copy the Forge JAR to `.minecraft/mods/` folder
4. Launch Minecraft with Forge profile

### For Minecraft 1.20.1

#### Fabric
1. Install Minecraft 1.20.1
2. Install Fabric Loader 0.16.10+
3. Download and install Fabric API 0.92.2+1.20.1
4. Copy the Fabric JAR to `.minecraft/mods/` folder
5. Launch Minecraft with Fabric profile

#### Forge
1. Install Minecraft 1.20.1
2. Install Forge 47.3.0+
3. Copy the Forge JAR to `.minecraft/mods/` folder
4. Launch Minecraft with Forge profile

## Project Structure

```
CastleShift/
├── common/
│   ├── shared/              # Shared version-agnostic sources (included via srcDir)
│   ├── 1.21.1/              # Common module for MC 1.21.1
│   │   └── src/main/
│   │       ├── java/com/castleshift/
│   │       │   ├── CastleShift.java       # Common entry point
│   │       │   ├── processor/             # Structure processors
│   │       │   └── worldgen/              # World generation
│   │       └── resources/
│   │           ├── data/castleshift/      # Structures, worldgen config
│   │           └── assets/castleshift/    # Textures, lang files
│   └── 1.20.1/              # Common module for MC 1.20.1
├── fabric/
│   ├── base/                # Shared Fabric sources
│   ├── 1.21.1/              # Fabric subproject for MC 1.21.1
│   └── 1.20.1/              # Fabric subproject for MC 1.20.1
├── neoforge/
│   ├── base/                # Shared NeoForge sources
│   └── 1.21.1/              # NeoForge subproject for MC 1.21.1
├── forge/
│   ├── base/                # Shared Forge sources
│   ├── base-56/             # Forge 56+ sources (MC 1.21.6+, EventBus 7)
│   ├── 1.21.1/              # Forge subproject for MC 1.21.1 (ForgeGradle)
│   └── 1.20.1/              # Forge subproject for MC 1.20.1 (Architectury Loom)
├── props/                   # Version-specific properties
├── scripts/                 # Build and release scripts
├── build.gradle             # Root build configuration (Groovy DSL)
├── settings.gradle          # Multi-module settings
└── gradle.properties        # Version configuration
```

## Technical Notes

- **Build DSL**: Groovy DSL (for Architectury Loom compatibility)
- **Mappings**: Mojang mappings (official Minecraft class names)
- **Shadow Plugin**: Bundles common module into loader-specific JARs
- **Structure Files**: NBT format, placed in `common/{version}/src/main/resources/data/castleshift/structure/`
- **NBT Conversion**: Automatic 1.21.1 -> 1.20.1 structure conversion via build script

## License

This project is licensed under the **GNU Lesser General Public License v3.0 (LGPL-3.0)**.

Copyright (C) 2026 Soichiro Kashima

See the [COPYING](COPYING) and [COPYING.LESSER](COPYING.LESSER) files for full license text.

## Credits

- Built with [Architectury Loom](https://github.com/architectury/architectury-loom) and [ForgeGradle](https://github.com/MinecraftForge/ForgeGradle)

## Support

For issues, feature requests, or questions:
- Open an issue on [GitHub Issues](https://github.com/ksoichiro/CastleShift/issues)

---

**Developed for Minecraft Java Edition 1.21.1 / 1.20.1**
