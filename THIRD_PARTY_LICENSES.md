# Third-Party Licenses

This document lists all third-party dependencies used in Castle Shift and their respective licenses.

## Runtime Dependencies

### Minecraft & Core Framework

#### Minecraft
- **Project**: Minecraft Java Edition
- **Version**: 1.21.1
- **Developer**: Mojang Studios
- **License**: Minecraft EULA
- **URL**: https://www.minecraft.net/

#### Fabric Loader (Fabric)
- **Project**: Fabric Loader
- **Version**: 0.17.3+
- **Organization**: FabricMC
- **License**: Apache License 2.0
- **URL**: https://github.com/FabricMC/fabric-loader
- **License URL**: https://github.com/FabricMC/fabric-loader/blob/master/LICENSE

#### Fabric API (Fabric)
- **Project**: Fabric API
- **Version**: 0.116.7+1.21.1
- **Organization**: FabricMC
- **License**: Apache License 2.0
- **URL**: https://github.com/FabricMC/fabric
- **License URL**: https://github.com/FabricMC/fabric/blob/master/LICENSE

#### NeoForge (NeoForge)
- **Project**: NeoForge
- **Version**: 21.1.209+
- **Organization**: NeoForged
- **License**: LGPL-2.1-only
- **URL**: https://github.com/neoforged/NeoForge
- **License URL**: https://github.com/neoforged/NeoForge/blob/1.21.x/LICENSE.txt
- **Note**: Projects using NeoForge's APIs are not required to be licensed under LGPL-2.1

#### Forge (Forge, 1.20.1)
- **Project**: Minecraft Forge
- **Version**: 47.3.0
- **Organization**: MinecraftForge
- **License**: LGPL-2.1-only
- **URL**: https://github.com/MinecraftForge/MinecraftForge
- **License URL**: https://github.com/MinecraftForge/MinecraftForge/blob/1.20.x/LICENSE.txt
- **Note**: Projects using Forge's APIs are not required to be licensed under LGPL-2.1

## Development Dependencies

### Build Tools

#### Architectury Loom
- **Project**: Architectury Loom (Gradle Plugin)
- **Version**: 1.13-SNAPSHOT
- **Organization**: Architectury
- **License**: MIT License
- **URL**: https://github.com/architectury/architectury-loom

#### Architectury Plugin
- **Project**: Architectury Plugin (Gradle Plugin)
- **Version**: 3.4-SNAPSHOT
- **Organization**: Architectury
- **License**: MIT License
- **URL**: https://github.com/architectury/architectury-plugin

#### Gradle Shadow Plugin
- **Project**: Gradle Shadow
- **Version**: 8.3.6
- **Organization**: GradleUp
- **License**: Apache License 2.0
- **URL**: https://github.com/GradleUp/shadow

## License Summaries

### Apache License 2.0
Permissive license that allows commercial use, modification, distribution, and private use. Requires preservation of copyright and license notices.

**Used by**: Fabric Loader, Fabric API, Gradle Shadow Plugin

### LGPL-2.1-only
Copyleft license that requires derivative works to be licensed under LGPL-2.1. However, linking to libraries licensed under LGPL-2.1 does not require the linking code to be licensed under LGPL-2.1.

**Used by**: NeoForge, Forge

### MIT License
Very permissive license allowing nearly unrestricted use, modification, and distribution. Only requires preservation of copyright and license notices.

**Used by**: Architectury Loom, Architectury Plugin

## Notes

- All dependencies are used in compliance with their respective licenses
- Runtime dependencies (mod loaders, Fabric API) are not bundled with Castle Shift; users must install them separately
- NeoForge's and Forge's LGPL-2.1 license does not affect Castle Shift's license due to linking exception

## License Compliance

Castle Shift is licensed under LGPL-3.0-only. All dependencies are compatible with this license:
- **Permissive licenses** (Apache 2.0, MIT): Fully compatible
- **LGPL licenses** (LGPL-2.1): Compatible due to dynamic linking (no license propagation)

For questions about licensing, please contact the project maintainer.

---

Last updated: 2026-02-26
