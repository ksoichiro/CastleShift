# CastleShift

A structure mod for Minecraft that adds castles to the world.

Built with [Architectury](https://docs.architectury.dev/) for multi-loader, multi-version support.

- **1.21.1**: Fabric + NeoForge
- **1.20.1**: Fabric + Forge

## Build Commands

```bash
# Build for default version (target_mc_version in gradle.properties)
./gradlew build

# Build for a specific version
./gradlew build -Ptarget_mc_version=1.21.1
./gradlew build -Ptarget_mc_version=1.20.1

# Run client
./gradlew fabric:runClient
./gradlew neoforge:runClient -Ptarget_mc_version=1.21.1
./gradlew forge:runClient -Ptarget_mc_version=1.20.1

# Multi-version tasks
./gradlew buildAll       # Build all versions
./gradlew cleanAll       # Clean all versions
./gradlew release        # cleanAll + buildAll + collectJars
```

## Directory Structure

```
.
├── common/
│   ├── shared/             # Version-agnostic shared code
│   ├── 1.21.1/             # Common code for MC 1.21.1
│   └── 1.20.1/             # Common code for MC 1.20.1
├── fabric/
│   ├── base/               # Shared Fabric code
│   ├── 1.21.1/             # Fabric for MC 1.21.1
│   └── 1.20.1/             # Fabric for MC 1.20.1
├── neoforge/
│   ├── base/               # Shared NeoForge code
│   └── 1.21.1/             # NeoForge for MC 1.21.1
└── forge/
    ├── base/               # Shared Forge code
    └── 1.20.1/             # Forge for MC 1.20.1
```

## License

LGPL-3.0-only
