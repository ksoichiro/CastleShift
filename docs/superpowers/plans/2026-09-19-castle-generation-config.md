# Castle Generation Config (1.21.1 Vertical Slice) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a TOML config file plus an in-game settings screen that let a player/operator enable, disable, and control the generation frequency (preset or numeric override) of `castleshift:castle`, on MC 1.21.1 across Fabric, NeoForge, and Forge, without requiring a world restart to pick a different datapack.

**Architecture:** A version-specific `config` package (mirrors the existing per-version `world/processor` package) holds an immutable config record, TOML load/save, and a vanilla-widget settings screen. A new custom `StructurePlacementType` (`castleshift:configurable_spread`) replaces `minecraft:random_spread` in `castle.json` and reads spacing/separation/enabled from that config at chunk-generation time instead of from the JSON, registered per-loader exactly like the existing `StructureProcessor` types.

**Tech Stack:** Java 21 (Minecraft 1.21.1), `com.electronwill.night-config` (TOML, bundled via Fabric `include`, `compileOnly` on NeoForge/Forge), vanilla `Screen`/`ContainerObjectSelectionList` widgets (no Cloth Config/ModMenu hard dependency).

**Spec:** `docs/superpowers/specs/2026-09-19-castle-generation-config-design.md`

## Global Constraints

- Config is server-authoritative; the screen must show a warning banner when `minecraft.level != null && !minecraft.hasSingleplayerServer()`, since edits only affect the local player's view of a remote server.
- `customSpacing`/`customSeparation` are independent nullable overrides; either, both, or neither may be set; a non-null value always wins over the active preset for that field.
- Effective `separation < spacing` must always hold; `ConfigLoader` clamps `customSeparation` down to `customSpacing - 1` rather than rejecting the file.
- No new mandatory dependency: ModMenu stays `compileOnly`/optional; night-config is `compileOnly` on NeoForge/Forge (loader-provided), `include`d on Fabric.
- This plan targets **1.21.1 only** (Fabric + NeoForge + Forge). Porting to other supported versions (1.20.1, 1.21.2+, 26.x) is explicitly out of scope and tracked as follow-up work, per the project's established practice of doing one-version vertical slices first.
- Registration must follow the frozen-registry rule already documented in CLAUDE.md: Fabric registers directly (`Registry.register` in an `init()` method), NeoForge/Forge use `DeferredRegister` on the mod event bus — never `Registry.register` in a NeoForge/Forge constructor.

---

## File Structure

```
common/1.21.1/src/main/java/com/castleshift/config/
├── CastleShiftConfig.java       # immutable record + static get/set holder
├── ConfigDefaults.java          # preset constants + defaults()
├── ConfigRanges.java            # min/max + separation<spacing validation
├── ConfigLoader.java            # night-config TOML read, clamp/fallback, schema_version
├── ConfigWriter.java            # write-back preserving comments
└── client/
    └── ConfigScreen.java        # vanilla-widget settings screen

common/1.21.1/src/main/java/com/castleshift/world/placement/
├── ConfigurableSpreadStructurePlacement.java
└── ModPlacements.java            # StructurePlacementType constant + Fabric init()

common/1.21.1/src/main/resources/castleshift.toml   # bundled default config resource
common/1.21.1/src/main/resources/data/castleshift/worldgen/structure_set/castle.json  # modified

common/1.21.1/src/test/java/com/castleshift/config/
├── ConfigLoaderTest.java
└── ConfigWriterTest.java
common/1.21.1/src/test/java/com/castleshift/world/placement/
└── ConfigurableSpreadStructurePlacementTest.java

fabric/base/src/main/java/com/castleshift/fabric/CastleShiftFabric.java     # modify: call ModPlacements.init(), register KeyMapping
fabric/base-modmenu/src/main/java/com/castleshift/fabric/modmenu/CastleShiftModMenuIntegration.java  # new optional module
neoforge/base/src/main/java/com/castleshift/neoforge/CastleShiftNeoForge.java  # modify: register placement DeferredRegister, config screen factory
forge/base/src/main/java/com/castleshift/forge/CastleShiftForge.java          # modify: same for Forge 47-55 API

props/1.21.1.properties          # add night-config version property if not already present
common/1.21.1/build.gradle       # add night-config compileOnly dependency
fabric/1.21.1/build.gradle       # add night-config include dependency, add base-modmenu srcDir + ModMenu compileOnly
neoforge/1.21.1/build.gradle     # add night-config compileOnly (should already be present transitively; verify)
forge/1.21.1/build.gradle        # add night-config compileOnly (should already be present transitively; verify)

docs/future-expansion-plan.md    # update "Generation Frequency and Compatibility" section
```

---

### Task 1: Config data model (`CastleShiftConfig`, `ConfigDefaults`, `ConfigRanges`)

**Files:**
- Create: `common/1.21.1/src/main/java/com/castleshift/config/CastleShiftConfig.java`
- Create: `common/1.21.1/src/main/java/com/castleshift/config/ConfigDefaults.java`
- Create: `common/1.21.1/src/main/java/com/castleshift/config/ConfigRanges.java`
- Test: `common/1.21.1/src/test/java/com/castleshift/config/CastleShiftConfigTest.java`

**Interfaces:**
- Produces: `CastleShiftConfig` (record), `CastleShiftConfig.Preset` (enum: `SPARSE`, `DEFAULT`, `FREQUENT`), `CastleShiftConfig.Generation` (record: `enabled: boolean`, `preset: Preset`, `customSpacing: Integer` nullable, `customSeparation: Integer` nullable), `CastleShiftConfig.Generation#effectiveSpacing(): int`, `CastleShiftConfig.Generation#effectiveSeparation(): int`, `CastleShiftConfig#get(): CastleShiftConfig`, `CastleShiftConfig#set(CastleShiftConfig): void`.
- `ConfigDefaults#defaults(): CastleShiftConfig`, `ConfigDefaults.SPARSE_SPACING/SEPARATION`, `DEFAULT_SPACING/SEPARATION`, `FREQUENT_SPACING/SEPARATION` (int constants: 64/16, 32/8, 16/4).
- `ConfigRanges.MIN_SPACING=8`, `MAX_SPACING=512`, `MIN_SEPARATION=1`, `MAX_SEPARATION=256`, `ConfigRanges#clampSeparation(int spacing, int separation): int` (returns `Math.min(separation, spacing - 1)`, then clamps to `[MIN_SEPARATION, MAX_SEPARATION]`).

- [ ] **Step 1: Write the failing test for `Generation#effectiveSpacing`/`effectiveSeparation` precedence**

```java
package com.castleshift.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CastleShiftConfigTest {

    @Test
    void effectiveValuesUsePresetWhenNoOverride() {
        CastleShiftConfig.Generation gen =
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.SPARSE, null, null);
        assertEquals(ConfigDefaults.SPARSE_SPACING, gen.effectiveSpacing());
        assertEquals(ConfigDefaults.SPARSE_SEPARATION, gen.effectiveSeparation());
    }

    @Test
    void customSpacingOverridesPresetButSeparationFallsBackToPreset() {
        CastleShiftConfig.Generation gen =
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, 100, null);
        assertEquals(100, gen.effectiveSpacing());
        assertEquals(ConfigDefaults.DEFAULT_SEPARATION, gen.effectiveSeparation());
    }

    @Test
    void bothCustomValuesOverridePreset() {
        CastleShiftConfig.Generation gen =
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.FREQUENT, 40, 5);
        assertEquals(40, gen.effectiveSpacing());
        assertEquals(5, gen.effectiveSeparation());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew common-1.21.1:test --tests com.castleshift.config.CastleShiftConfigTest -Ptarget_mc_version=1.21.1`
Expected: FAIL — `com.castleshift.config` package / `CastleShiftConfig` does not exist (compile error).

- [ ] **Step 3: Write `ConfigDefaults`**

```java
package com.castleshift.config;

public final class ConfigDefaults {
    public static final int SPARSE_SPACING = 64;
    public static final int SPARSE_SEPARATION = 16;
    public static final int DEFAULT_SPACING = 32;
    public static final int DEFAULT_SEPARATION = 8;
    public static final int FREQUENT_SPACING = 16;
    public static final int FREQUENT_SEPARATION = 4;
    public static final int CURRENT_SCHEMA_VERSION = 1;

    private ConfigDefaults() {}

    public static CastleShiftConfig defaults() {
        return new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, null, null));
    }
}
```

- [ ] **Step 4: Write `ConfigRanges`**

```java
package com.castleshift.config;

public final class ConfigRanges {
    public static final int MIN_SPACING = 8;
    public static final int MAX_SPACING = 512;
    public static final int MIN_SEPARATION = 1;
    public static final int MAX_SEPARATION = 256;

    private ConfigRanges() {}

    public static int clampSpacing(int spacing) {
        return Math.max(MIN_SPACING, Math.min(MAX_SPACING, spacing));
    }

    public static int clampSeparation(int spacing, int separation) {
        int bounded = Math.max(MIN_SEPARATION, Math.min(MAX_SEPARATION, separation));
        return Math.min(bounded, spacing - 1);
    }
}
```

- [ ] **Step 5: Write `CastleShiftConfig`**

```java
package com.castleshift.config;

public record CastleShiftConfig(Generation generation) {

    private static volatile CastleShiftConfig instance = ConfigDefaults.defaults();

    public static CastleShiftConfig get() {
        return instance;
    }

    public static void set(CastleShiftConfig config) {
        instance = config;
    }

    public enum Preset {
        SPARSE,
        DEFAULT,
        FREQUENT
    }

    public record Generation(boolean enabled, Preset preset, Integer customSpacing, Integer customSeparation) {

        public int effectiveSpacing() {
            if (customSpacing != null) {
                return customSpacing;
            }
            return switch (preset) {
                case SPARSE -> ConfigDefaults.SPARSE_SPACING;
                case DEFAULT -> ConfigDefaults.DEFAULT_SPACING;
                case FREQUENT -> ConfigDefaults.FREQUENT_SPACING;
            };
        }

        public int effectiveSeparation() {
            if (customSeparation != null) {
                return customSeparation;
            }
            return switch (preset) {
                case SPARSE -> ConfigDefaults.SPARSE_SEPARATION;
                case DEFAULT -> ConfigDefaults.DEFAULT_SEPARATION;
                case FREQUENT -> ConfigDefaults.FREQUENT_SEPARATION;
            };
        }
    }
}
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `./gradlew common-1.21.1:test --tests com.castleshift.config.CastleShiftConfigTest -Ptarget_mc_version=1.21.1`
Expected: PASS (3 tests)

- [ ] **Step 7: Commit**

```bash
git add common/1.21.1/src/main/java/com/castleshift/config/CastleShiftConfig.java \
        common/1.21.1/src/main/java/com/castleshift/config/ConfigDefaults.java \
        common/1.21.1/src/main/java/com/castleshift/config/ConfigRanges.java \
        common/1.21.1/src/test/java/com/castleshift/config/CastleShiftConfigTest.java
git commit -m "feat(config): add castle generation config data model"
```

---

### Task 2: night-config dependency wiring

**Files:**
- Modify: `common/1.21.1/build.gradle`
- Modify: `fabric/1.21.1/build.gradle`
- Modify: `neoforge/1.21.1/build.gradle`
- Modify: `forge/1.21.1/build.gradle`

**Interfaces:**
- Consumes: none (build config only).
- Produces: `com.electronwill.nightconfig.core.file.FileConfig` / `CommentedFileConfig` available on the `common-1.21.1` compile classpath and at runtime on every 1.21.1 platform module.

- [ ] **Step 1: Check the night-config version already resolved on the loader classpaths**

Run: `find ~/.gradle/caches -iname "night-config*1.21.1*" -o -iname "*night-config-toml*" 2>/dev/null | grep -o "night-config-toml-[0-9.]*" | sort -u`
Expected: at least one version string (e.g. `night-config-toml-3.6.6`); use that exact version below (loader-shipped version must match what NeoForge/Forge actually provide, since `compileOnly` will not download it — it must already be on the runtime classpath from the loader).

- [ ] **Step 2: Add `compileOnly` dependency to `common/1.21.1/build.gradle`**

Add inside the existing `dependencies { ... }` block (do not create a new block):

```groovy
    compileOnly "com.electronwill.night-config:core:${night_config_version}"
    compileOnly "com.electronwill.night-config:toml:${night_config_version}"
```

Add `night_config_version=3.6.6` (or whatever version Step 1 found) to `gradle.properties` at the repo root, next to the other shared version properties.

- [ ] **Step 3: Add `include` dependency for Fabric, `compileOnly` for NeoForge/Forge**

`fabric/1.21.1/build.gradle`, inside `dependencies { ... }`:

```groovy
    include "com.electronwill.night-config:core:${night_config_version}"
    include "com.electronwill.night-config:toml:${night_config_version}"
```

`neoforge/1.21.1/build.gradle` and `forge/1.21.1/build.gradle`, inside `dependencies { ... }`:

```groovy
    compileOnly "com.electronwill.night-config:core:${night_config_version}"
    compileOnly "com.electronwill.night-config:toml:${night_config_version}"
```

- [ ] **Step 4: Verify the build resolves on all three loaders**

Run: `./gradlew fabric:compileJava neoforge:compileJava forge:compileJava -Ptarget_mc_version=1.21.1`
Expected: BUILD SUCCESSFUL (no unresolved dependency errors; no code uses the new classes yet, so this only validates dependency resolution).

- [ ] **Step 5: Commit**

```bash
git add gradle.properties common/1.21.1/build.gradle fabric/1.21.1/build.gradle neoforge/1.21.1/build.gradle forge/1.21.1/build.gradle
git commit -m "build(1.21.1): add night-config dependency for castle generation config"
```

---

### Task 3: `ConfigLoader` and `ConfigWriter`

**Files:**
- Create: `common/1.21.1/src/main/java/com/castleshift/config/ConfigLoader.java`
- Create: `common/1.21.1/src/main/java/com/castleshift/config/ConfigWriter.java`
- Create: `common/1.21.1/src/main/resources/castleshift.toml`
- Test: `common/1.21.1/src/test/java/com/castleshift/config/ConfigLoaderTest.java`
- Test: `common/1.21.1/src/test/java/com/castleshift/config/ConfigWriterTest.java`

**Interfaces:**
- Consumes: `CastleShiftConfig`, `CastleShiftConfig.Generation`, `CastleShiftConfig.Preset`, `ConfigDefaults.defaults()`, `ConfigDefaults.CURRENT_SCHEMA_VERSION`, `ConfigRanges.clampSpacing`, `ConfigRanges.clampSeparation` (all from Task 1).
- Produces: `ConfigLoader.load(Path configFile): CastleShiftConfig` (creates the file with bundled defaults if absent, clamps/falls back invalid fields, warns and fills gaps on schema mismatch), `ConfigWriter.save(Path configFile, CastleShiftConfig config): void` (loads existing `CommentedFileConfig`, sets fields, saves — preserves comments/formatting of an existing file).

- [ ] **Step 1: Write the bundled default resource**

`common/1.21.1/src/main/resources/castleshift.toml`:

```toml
# Castle Shift generation settings.
schema_version = 1

[generation]
    # Whether castles generate at all.
    enabled = true
    # One of: SPARSE, DEFAULT, FREQUENT.
    preset = "DEFAULT"
    # Overrides the preset's spacing/separation when set. Remove the line
    # (or leave commented) to use the preset's value.
    #custom_spacing = 32
    #custom_separation = 8
```

- [ ] **Step 2: Write the failing test for `ConfigLoader`**

```java
package com.castleshift.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigLoaderTest {

    @TempDir Path tempDir;

    @Test
    void createsDefaultFileWhenMissing() {
        Path configFile = tempDir.resolve("castleshift.toml");
        CastleShiftConfig loaded = ConfigLoader.load(configFile);

        assertEquals(ConfigDefaults.defaults(), loaded);
        assertTrue(Files.exists(configFile));
    }

    @Test
    void clampsOutOfRangeSpacing() throws IOException {
        Path configFile = tempDir.resolve("castleshift.toml");
        Files.writeString(
                configFile,
                """
                schema_version = 1
                [generation]
                    enabled = true
                    preset = "DEFAULT"
                    custom_spacing = 999999
                """);

        CastleShiftConfig loaded = ConfigLoader.load(configFile);

        assertEquals(ConfigRanges.MAX_SPACING, loaded.generation().customSpacing());
    }

    @Test
    void clampsSeparationBelowSpacingWhenBothCustom() throws IOException {
        Path configFile = tempDir.resolve("castleshift.toml");
        Files.writeString(
                configFile,
                """
                schema_version = 1
                [generation]
                    enabled = true
                    preset = "DEFAULT"
                    custom_spacing = 10
                    custom_separation = 50
                """);

        CastleShiftConfig loaded = ConfigLoader.load(configFile);

        assertEquals(10, loaded.generation().customSpacing());
        assertEquals(9, loaded.generation().customSeparation());
    }

    @Test
    void fallsBackToDefaultsOnUnparseablePreset() throws IOException {
        Path configFile = tempDir.resolve("castleshift.toml");
        Files.writeString(
                configFile,
                """
                schema_version = 1
                [generation]
                    enabled = true
                    preset = "NOT_A_PRESET"
                """);

        CastleShiftConfig loaded = ConfigLoader.load(configFile);

        assertEquals(CastleShiftConfig.Preset.DEFAULT, loaded.generation().preset());
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew common-1.21.1:test --tests com.castleshift.config.ConfigLoaderTest -Ptarget_mc_version=1.21.1`
Expected: FAIL — `ConfigLoader` does not exist (compile error).

- [ ] **Step 4: Write `ConfigLoader`**

```java
package com.castleshift.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigLoader {

    private ConfigLoader() {}

    public static CastleShiftConfig load(Path configFile) {
        if (!Files.exists(configFile)) {
            copyBundledDefault(configFile);
        }

        try (CommentedFileConfig fileConfig = CommentedFileConfig.of(configFile)) {
            fileConfig.load();

            int schemaVersion = fileConfig.getIntOrElse("schema_version", ConfigDefaults.CURRENT_SCHEMA_VERSION);
            if (schemaVersion != ConfigDefaults.CURRENT_SCHEMA_VERSION) {
                System.err.println(
                        "[castleshift] config schema_version " + schemaVersion
                                + " does not match expected " + ConfigDefaults.CURRENT_SCHEMA_VERSION
                                + "; missing fields will use defaults.");
            }

            CastleShiftConfig.Generation defaultsGen = ConfigDefaults.defaults().generation();

            boolean enabled = fileConfig.getOrElse("generation.enabled", defaultsGen.enabled());

            CastleShiftConfig.Preset preset = parsePreset(
                    fileConfig.getOrElse("generation.preset", defaultsGen.preset().name()), defaultsGen.preset());

            Integer customSpacing = readNullableInt(fileConfig, "generation.custom_spacing");
            Integer customSeparation = readNullableInt(fileConfig, "generation.custom_separation");

            if (customSpacing != null) {
                customSpacing = ConfigRanges.clampSpacing(customSpacing);
            }
            if (customSeparation != null) {
                int spacingForClamp = customSpacing != null ? customSpacing : new CastleShiftConfig.Generation(
                                enabled, preset, null, null)
                        .effectiveSpacing();
                customSeparation = ConfigRanges.clampSeparation(spacingForClamp, customSeparation);
            }

            return new CastleShiftConfig(
                    new CastleShiftConfig.Generation(enabled, preset, customSpacing, customSeparation));
        }
    }

    private static CastleShiftConfig.Preset parsePreset(String raw, CastleShiftConfig.Preset fallback) {
        try {
            return CastleShiftConfig.Preset.valueOf(raw);
        } catch (IllegalArgumentException e) {
            System.err.println("[castleshift] invalid generation.preset '" + raw + "'; falling back to " + fallback);
            return fallback;
        }
    }

    private static Integer readNullableInt(FileConfig fileConfig, String path) {
        Number value = fileConfig.get(path);
        return value == null ? null : value.intValue();
    }

    private static void copyBundledDefault(Path configFile) {
        try {
            Files.createDirectories(configFile.getParent());
            try (InputStream in = ConfigLoader.class.getResourceAsStream("/castleshift.toml")) {
                if (in == null) {
                    throw new IllegalStateException("Bundled castleshift.toml resource is missing");
                }
                Files.copy(in, configFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

- [ ] **Step 5: Run tests to verify `ConfigLoaderTest` passes**

Run: `./gradlew common-1.21.1:test --tests com.castleshift.config.ConfigLoaderTest -Ptarget_mc_version=1.21.1`
Expected: PASS (4 tests)

- [ ] **Step 6: Write the failing test for `ConfigWriter`**

```java
package com.castleshift.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigWriterTest {

    @TempDir Path tempDir;

    @Test
    void writesValuesAndPreservesLeadingComment() throws Exception {
        Path configFile = tempDir.resolve("castleshift.toml");
        ConfigLoader.load(configFile); // seeds the file with the bundled default (incl. its header comment)

        CastleShiftConfig updated = new CastleShiftConfig(
                new CastleShiftConfig.Generation(false, CastleShiftConfig.Preset.FREQUENT, 20, 3));
        ConfigWriter.save(configFile, updated);

        CastleShiftConfig reloaded = ConfigLoader.load(configFile);
        assertEquals(updated, reloaded);

        String contents = Files.readString(configFile);
        assertTrue(contents.contains("Castle Shift generation settings"));
    }
}
```

- [ ] **Step 7: Run test to verify it fails**

Run: `./gradlew common-1.21.1:test --tests com.castleshift.config.ConfigWriterTest -Ptarget_mc_version=1.21.1`
Expected: FAIL — `ConfigWriter` does not exist (compile error).

- [ ] **Step 8: Write `ConfigWriter`**

```java
package com.castleshift.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Path;

public final class ConfigWriter {

    private ConfigWriter() {}

    public static void save(Path configFile, CastleShiftConfig config) {
        try (CommentedFileConfig fileConfig = CommentedFileConfig.of(configFile)) {
            fileConfig.load();

            CastleShiftConfig.Generation gen = config.generation();
            fileConfig.set("schema_version", ConfigDefaults.CURRENT_SCHEMA_VERSION);
            fileConfig.set("generation.enabled", gen.enabled());
            fileConfig.set("generation.preset", gen.preset().name());
            if (gen.customSpacing() != null) {
                fileConfig.set("generation.custom_spacing", gen.customSpacing());
            } else {
                fileConfig.remove("generation.custom_spacing");
            }
            if (gen.customSeparation() != null) {
                fileConfig.set("generation.custom_separation", gen.customSeparation());
            } else {
                fileConfig.remove("generation.custom_separation");
            }

            fileConfig.save();
        }
    }
}
```

- [ ] **Step 9: Run tests to verify they pass**

Run: `./gradlew common-1.21.1:test --tests "com.castleshift.config.*" -Ptarget_mc_version=1.21.1`
Expected: PASS (all `ConfigLoaderTest` + `ConfigWriterTest` cases)

- [ ] **Step 10: Commit**

```bash
git add common/1.21.1/src/main/java/com/castleshift/config/ConfigLoader.java \
        common/1.21.1/src/main/java/com/castleshift/config/ConfigWriter.java \
        common/1.21.1/src/main/resources/castleshift.toml \
        common/1.21.1/src/test/java/com/castleshift/config/ConfigLoaderTest.java \
        common/1.21.1/src/test/java/com/castleshift/config/ConfigWriterTest.java
git commit -m "feat(config): add TOML load/save for castle generation config"
```

---

### Task 4: `ConfigurableSpreadStructurePlacement` and registration (all three loaders)

**Files:**
- Create: `common/1.21.1/src/main/java/com/castleshift/world/placement/ConfigurableSpreadStructurePlacement.java`
- Create: `common/1.21.1/src/main/java/com/castleshift/world/placement/ModPlacements.java`
- Test: `common/1.21.1/src/test/java/com/castleshift/world/placement/ConfigurableSpreadStructurePlacementTest.java`
- Modify: `fabric/base/src/main/java/com/castleshift/fabric/CastleShiftFabric.java`
- Modify: `neoforge/base/src/main/java/com/castleshift/neoforge/CastleShiftNeoForge.java`
- Modify: `forge/base/src/main/java/com/castleshift/forge/CastleShiftForge.java`
- Modify: `common/1.21.1/src/main/resources/data/castleshift/worldgen/structure_set/castle.json`

**Interfaces:**
- Consumes: `CastleShiftConfig.get()`, `CastleShiftConfig.Generation.effectiveSpacing()/effectiveSeparation()/enabled()` (Task 1).
- Produces: `ModPlacements.CONFIGURABLE_SPREAD: StructurePlacementType<ConfigurableSpreadStructurePlacement>`, `ModPlacements.init(): void` (Fabric-only registration entrypoint, mirrors `ModProcessors.init()`).

**Note before starting:** `StructurePlacement` (`net.minecraft.world.level.levelgen.structure.placement.StructurePlacement`) and `RandomSpreadStructurePlacement` are not used anywhere else in this codebase yet (confirmed: zero matches repo-wide). Before writing the class in Step 3, open `RandomSpreadStructurePlacement.class` for 1.21.1 in your IDE (source attached via the Loom/ForgeGradle-downloaded mapped jar) and confirm: the abstract method(s) you must implement on `StructurePlacement` (expected: `isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ): boolean` and `type(): StructurePlacementType<?>`), and the shared codec helper (expected: a protected static `placementCodec(RecordCodecBuilder.Instance<S>)` on `StructurePlacement` that `RandomSpreadStructurePlacement.CODEC` builds on top of). Adjust the skeleton below to match exactly what you find — the compiler will catch any mismatch in Step 5.

- [ ] **Step 1: Write the failing test for placement behavior**

```java
package com.castleshift.world.placement;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.castleshift.config.CastleShiftConfig;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ConfigurableSpreadStructurePlacementTest {

    @AfterEach
    void resetConfig() {
        CastleShiftConfig.set(com.castleshift.config.ConfigDefaults.defaults());
    }

    @Test
    void disabledConfigNeverProducesAPlacementChunk() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(false, CastleShiftConfig.Preset.DEFAULT, null, null)));

        ConfigurableSpreadStructurePlacement placement =
                PlacementTestInvoker.create(194572831);

        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                assertFalse(
                        PlacementTestInvoker.isPotentialSpreadChunk(placement, 0L, x, z),
                        "expected no placement chunk when generation is disabled");
            }
        }
    }

    @Test
    void enabledConfigUsesEffectiveSpacingFromPreset() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.FREQUENT, null, null)));

        ConfigurableSpreadStructurePlacement placement =
                PlacementTestInvoker.create(194572831);

        assertFalse(placement.spacing() == com.castleshift.config.ConfigDefaults.DEFAULT_SPACING);
    }

    @Test
    void customSpacingOverridesPreset() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, 77, null)));

        ConfigurableSpreadStructurePlacement placement =
                PlacementTestInvoker.create(194572831);

        org.junit.jupiter.api.Assertions.assertEquals(77, placement.spacing());
    }
}
```

Create the accompanying test-only helper (this repo's established pattern for absorbing version-specific construction/signature differences, per `ProcessorTestInvoker`):

```java
package com.castleshift.world.placement;

import net.minecraft.world.level.levelgen.ChunkGeneratorStructureState;

final class PlacementTestInvoker {
    private PlacementTestInvoker() {}

    static ConfigurableSpreadStructurePlacement create(int salt) {
        return ConfigurableSpreadStructurePlacement.of(salt);
    }

    static boolean isPotentialSpreadChunk(
            ConfigurableSpreadStructurePlacement placement, long seed, int chunkX, int chunkZ) {
        return placement.isPotentialSpreadChunk(seed, chunkX, chunkZ);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew common-1.21.1:test --tests "com.castleshift.world.placement.*" -Ptarget_mc_version=1.21.1`
Expected: FAIL — `ConfigurableSpreadStructurePlacement` does not exist (compile error).

- [ ] **Step 3: Write `ConfigurableSpreadStructurePlacement`**

Reads live config on every call instead of storing spacing/separation as final fields from the codec (deliberately not codec-sourced, since the whole point is that config — not the JSON — decides these values). Keep `salt`/`locateOffset`/`frequency`/`exclusionZone` behavior delegated to the vanilla base class exactly as `RandomSpreadStructurePlacement` does, per the IDE check above:

```java
package com.castleshift.world.placement;

import com.castleshift.config.CastleShiftConfig;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class ConfigurableSpreadStructurePlacement extends StructurePlacement {

    public static final MapCodec<ConfigurableSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
            instance -> placementCodec(instance)
                    .apply(instance, ConfigurableSpreadStructurePlacement::new));

    public ConfigurableSpreadStructurePlacement(
            Vec3i locateOffset,
            FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            java.util.Optional<ExclusionZone> exclusionZone) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
    }

    public static ConfigurableSpreadStructurePlacement of(int salt) {
        return new ConfigurableSpreadStructurePlacement(
                Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, salt, java.util.Optional.empty());
    }

    public int spacing() {
        return CastleShiftConfig.get().generation().effectiveSpacing();
    }

    public int separation() {
        return CastleShiftConfig.get().generation().effectiveSeparation();
    }

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        if (!CastleShiftConfig.get().generation().enabled()) {
            return false;
        }
        ChunkPos potential = getPotentialStructureChunk(state.getLevelSeed(), chunkX, chunkZ);
        return potential.x == chunkX && potential.z == chunkZ && state.hasStructureChunkInRange(this, chunkX, chunkZ);
    }

    boolean isPotentialSpreadChunk(long seed, int chunkX, int chunkZ) {
        if (!CastleShiftConfig.get().generation().enabled()) {
            return false;
        }
        ChunkPos potential = getPotentialStructureChunk(seed, chunkX, chunkZ);
        return potential.x == chunkX && potential.z == chunkZ;
    }

    private ChunkPos getPotentialStructureChunk(long seed, int chunkX, int chunkZ) {
        int spacing = spacing();
        int separation = separation();
        int regionX = Math.floorDiv(chunkX, spacing);
        int regionZ = Math.floorDiv(chunkZ, spacing);
        RandomSource random = RandomSource.create();
        random.setLargeFeatureWithSalt(seed, regionX, regionZ, salt);
        int offsetRange = spacing - separation;
        int offsetX = random.nextInt(offsetRange);
        int offsetZ = random.nextInt(offsetRange);
        return new ChunkPos(regionX * spacing + offsetX, regionZ * spacing + offsetZ);
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModPlacements.CONFIGURABLE_SPREAD;
    }
}
```

- [ ] **Step 4: Write `ModPlacements`**

```java
package com.castleshift.world.placement;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class ModPlacements {
    public static final StructurePlacementType<ConfigurableSpreadStructurePlacement> CONFIGURABLE_SPREAD =
            () -> ConfigurableSpreadStructurePlacement.CODEC;

    public static void init() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("castleshift", "configurable_spread");
        if (!BuiltInRegistries.STRUCTURE_PLACEMENT.containsKey(id)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, id, CONFIGURABLE_SPREAD);
        }
    }
}
```

- [ ] **Step 5: Run tests to verify they pass, fixing any signature mismatch found via the IDE check**

Run: `./gradlew common-1.21.1:test --tests "com.castleshift.world.placement.*" -Ptarget_mc_version=1.21.1`
Expected: PASS (3 tests)

- [ ] **Step 6: Register on Fabric — modify `CastleShiftFabric.java`**

```java
package com.castleshift.fabric;

import com.castleshift.CastleShift;
import com.castleshift.world.placement.ModPlacements;
import com.castleshift.world.processor.ModProcessors;
import net.fabricmc.api.ModInitializer;

public class CastleShiftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModProcessors.init();
        ModPlacements.init();
        CastleShift.init();
    }
}
```

- [ ] **Step 7: Register on NeoForge — modify `CastleShiftNeoForge.java`**

```java
package com.castleshift.neoforge;

import com.castleshift.CastleShift;
import com.castleshift.world.placement.ModPlacements;
import com.castleshift.world.processor.ModProcessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CastleShift.MOD_ID)
public class CastleShiftNeoForge {
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, CastleShift.MOD_ID);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENT_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, CastleShift.MOD_ID);

    static {
        PROCESSOR_TYPES.register("roof_material", () -> ModProcessors.ROOF_MATERIAL);
        PROCESSOR_TYPES.register("wall_material", () -> ModProcessors.WALL_MATERIAL);
        PROCESSOR_TYPES.register("stair_material", () -> ModProcessors.STAIR_MATERIAL);
        PROCESSOR_TYPES.register("wall_weathering", () -> ModProcessors.WALL_WEATHERING);
        PLACEMENT_TYPES.register("configurable_spread", () -> ModPlacements.CONFIGURABLE_SPREAD);
    }

    public CastleShiftNeoForge(IEventBus modEventBus) {
        PROCESSOR_TYPES.register(modEventBus);
        PLACEMENT_TYPES.register(modEventBus);
        CastleShift.init();
    }
}
```

- [ ] **Step 8: Register on Forge — modify `CastleShiftForge.java`**

```java
package com.castleshift.forge;

import com.castleshift.CastleShift;
import com.castleshift.world.placement.ModPlacements;
import com.castleshift.world.processor.ModProcessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

@Mod(CastleShift.MOD_ID)
public class CastleShiftForge {
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, CastleShift.MOD_ID);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENT_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, CastleShift.MOD_ID);

    static {
        PROCESSOR_TYPES.register("roof_material", () -> ModProcessors.ROOF_MATERIAL);
        PROCESSOR_TYPES.register("wall_material", () -> ModProcessors.WALL_MATERIAL);
        PROCESSOR_TYPES.register("stair_material", () -> ModProcessors.STAIR_MATERIAL);
        PROCESSOR_TYPES.register("wall_weathering", () -> ModProcessors.WALL_WEATHERING);
        PLACEMENT_TYPES.register("configurable_spread", () -> ModPlacements.CONFIGURABLE_SPREAD);
    }

    public CastleShiftForge() {
        PROCESSOR_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PLACEMENT_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CastleShift.init();
    }
}
```

- [ ] **Step 9: Update `castle.json` to use the new placement type**

Modify `common/1.21.1/src/main/resources/data/castleshift/worldgen/structure_set/castle.json`:

```json
{
  "structures": [
    {
      "structure": "castleshift:castle",
      "weight": 1
    }
  ],
  "placement": {
    "type": "castleshift:configurable_spread",
    "salt": 194572831,
    "exclusion_zone": {
      "other_set": "minecraft:villages",
      "chunk_count": 10
    }
  }
}
```

- [ ] **Step 10: Build all three 1.21.1 platforms to confirm registration compiles and loads**

Run: `./gradlew fabric:build neoforge:build forge:build -Ptarget_mc_version=1.21.1`
Expected: BUILD SUCCESSFUL

- [ ] **Step 11: Commit**

```bash
git add common/1.21.1/src/main/java/com/castleshift/world/placement/ \
        common/1.21.1/src/test/java/com/castleshift/world/placement/ \
        common/1.21.1/src/main/resources/data/castleshift/worldgen/structure_set/castle.json \
        fabric/base/src/main/java/com/castleshift/fabric/CastleShiftFabric.java \
        neoforge/base/src/main/java/com/castleshift/neoforge/CastleShiftNeoForge.java \
        forge/base/src/main/java/com/castleshift/forge/CastleShiftForge.java
git commit -m "feat(worldgen): add config-driven structure placement type for castle generation"
```

---

### Task 5: Config screen (client)

**Files:**
- Create: `common/1.21.1/src/main/java/com/castleshift/config/client/ConfigScreen.java`
- Modify: `common/1.21.1/src/main/resources/assets/castleshift/lang/en_us.json`
- Modify: `common/1.21.1/src/main/resources/assets/castleshift/lang/ja_jp.json`

**Interfaces:**
- Consumes: `CastleShiftConfig`, `CastleShiftConfig.Generation`, `CastleShiftConfig.Preset`, `ConfigDefaults.defaults()`, `ConfigRanges` (Task 1), `ConfigLoader`/`ConfigWriter` file path convention `FabricLoader.getInstance().getConfigDir().resolve("castleshift.toml")`-equivalent — this task takes the config file `Path` as a constructor parameter so it stays loader-agnostic; each loader's entry point (Task 6) supplies its own config-dir path.
- Produces: `new ConfigScreen(Screen parent, Path configFile): Screen`.

- [ ] **Step 1: Write `ConfigScreen`**

There is no existing test-friendly way to unit test a `Screen` in this codebase (none of `ChronicleScreen`/`DeliveryStatusScreen`/MinersMarket's `ConfigScreen` have tests — they're verified manually per `future-expansion-plan.md`'s Verification Requirements). Write the screen directly, then verify manually in Task 7.

```java
package com.castleshift.config.client;

import com.castleshift.config.CastleShiftConfig;
import com.castleshift.config.ConfigDefaults;
import com.castleshift.config.ConfigLoader;
import com.castleshift.config.ConfigRanges;
import com.castleshift.config.ConfigWriter;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final Path configFile;

    private boolean enabled;
    private CastleShiftConfig.Preset preset;
    private EditBox customSpacingBox;
    private EditBox customSeparationBox;
    private Button doneButton;
    private boolean fieldsInvalid;

    public ConfigScreen(Screen parent, Path configFile) {
        super(Component.translatable("config.castleshift.title"));
        this.parent = parent;
        this.configFile = configFile;
        CastleShiftConfig.Generation gen = ConfigLoader.load(configFile).generation();
        this.enabled = gen.enabled();
        this.preset = gen.preset();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 4;

        if (this.minecraft.level != null && !this.minecraft.hasSingleplayerServer()) {
            // Non-authoritative warning: worldgen is server-side, so edits here only
            // affect this client's local copy, not the joined server's actual settings.
            y += 20;
        }

        this.addRenderableWidget(CycleButton.onOffBuilder(this.enabled)
                .create(centerX - 150, y, 300, 20, Component.translatable("config.castleshift.option.enabled"),
                        (button, value) -> this.enabled = value));

        y += 24;
        this.addRenderableWidget(CycleButton.<CastleShiftConfig.Preset>builder(p ->
                        Component.translatable("config.castleshift.preset." + p.name().toLowerCase()))
                .withValues(CastleShiftConfig.Preset.values())
                .withInitialValue(this.preset)
                .create(centerX - 150, y, 300, 20, Component.translatable("config.castleshift.option.preset"),
                        (button, value) -> this.preset = value));

        y += 24;
        this.customSpacingBox = new EditBox(this.font, centerX - 150, y, 145, 20,
                Component.translatable("config.castleshift.option.custom_spacing"));
        this.customSpacingBox.setResponder(text -> this.validate());
        this.addRenderableWidget(this.customSpacingBox);

        this.customSeparationBox = new EditBox(this.font, centerX + 5, y, 145, 20,
                Component.translatable("config.castleshift.option.custom_separation"));
        this.customSeparationBox.setResponder(text -> this.validate());
        this.addRenderableWidget(this.customSeparationBox);

        y += 32;
        this.addRenderableWidget(Button.builder(Component.translatable("config.castleshift.reset"),
                        button -> this.resetToDefaults())
                .bounds(centerX - 150, y, 95, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),
                        button -> this.minecraft.setScreen(this.parent))
                .bounds(centerX - 50, y, 95, 20)
                .build());
        this.doneButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        button -> this.saveAndClose())
                .bounds(centerX + 55, y, 95, 20)
                .build());

        this.validate();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String spacingText = this.customSpacingBox == null ? "" : this.customSpacingBox.getValue();
        String separationText = this.customSeparationBox == null ? "" : this.customSeparationBox.getValue();
        super.resize(minecraft, width, height);
        this.customSpacingBox.setValue(spacingText);
        this.customSeparationBox.setValue(separationText);
        this.validate();
    }

    private void validate() {
        this.fieldsInvalid = false;
        Integer spacing = parseNullableInt(this.customSpacingBox.getValue());
        Integer separation = parseNullableInt(this.customSeparationBox.getValue());
        if (spacing != null && (spacing < ConfigRanges.MIN_SPACING || spacing > ConfigRanges.MAX_SPACING)) {
            this.fieldsInvalid = true;
        }
        if (separation != null && (separation < ConfigRanges.MIN_SEPARATION || separation > ConfigRanges.MAX_SEPARATION)) {
            this.fieldsInvalid = true;
        }
        if (spacing != null && separation != null && separation >= spacing) {
            this.fieldsInvalid = true;
        }
        if (this.doneButton != null) {
            this.doneButton.active = !this.fieldsInvalid;
        }
    }

    private static Integer parseNullableInt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void resetToDefaults() {
        CastleShiftConfig.Generation defaults = ConfigDefaults.defaults().generation();
        this.enabled = defaults.enabled();
        this.preset = defaults.preset();
        this.customSpacingBox.setValue("");
        this.customSeparationBox.setValue("");
        this.validate();
    }

    private void saveAndClose() {
        Integer customSpacing = parseNullableInt(this.customSpacingBox.getValue());
        Integer customSeparation = parseNullableInt(this.customSeparationBox.getValue());
        CastleShiftConfig updated = new CastleShiftConfig(
                new CastleShiftConfig.Generation(this.enabled, this.preset, customSpacing, customSeparation));
        ConfigWriter.save(this.configFile, updated);
        CastleShiftConfig.set(updated);
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}
```

- [ ] **Step 2: Add lang keys**

Modify `common/1.21.1/src/main/resources/assets/castleshift/lang/en_us.json`, add:

```json
"config.castleshift.title": "Castle Shift Configuration",
"config.castleshift.option.enabled": "Generate Castles",
"config.castleshift.option.preset": "Generation Frequency",
"config.castleshift.preset.sparse": "Sparse",
"config.castleshift.preset.default": "Default",
"config.castleshift.preset.frequent": "Frequent",
"config.castleshift.option.custom_spacing": "Custom Spacing",
"config.castleshift.option.custom_separation": "Custom Separation",
"config.castleshift.reset": "Reset"
```

Modify `common/1.21.1/src/main/resources/assets/castleshift/lang/ja_jp.json`, add:

```json
"config.castleshift.title": "Castle Shift 設定",
"config.castleshift.option.enabled": "城を生成する",
"config.castleshift.option.preset": "生成頻度",
"config.castleshift.preset.sparse": "少なめ",
"config.castleshift.preset.default": "標準",
"config.castleshift.preset.frequent": "多め",
"config.castleshift.option.custom_spacing": "間隔(spacing)を直接指定",
"config.castleshift.option.custom_separation": "分離距離(separation)を直接指定",
"config.castleshift.reset": "リセット"
```

- [ ] **Step 3: Compile-check**

Run: `./gradlew common-1.21.1:compileJava -Ptarget_mc_version=1.21.1`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add common/1.21.1/src/main/java/com/castleshift/config/client/ConfigScreen.java \
        common/1.21.1/src/main/resources/assets/castleshift/lang/en_us.json \
        common/1.21.1/src/main/resources/assets/castleshift/lang/ja_jp.json
git commit -m "feat(config): add in-game config screen"
```

---

### Task 6: Entry points — NeoForge/Forge config button, Fabric ModMenu, keybinding

**Files:**
- Modify: `neoforge/base/src/main/java/com/castleshift/neoforge/CastleShiftNeoForge.java`
- Modify: `forge/base/src/main/java/com/castleshift/forge/CastleShiftForge.java`
- Create: `fabric/base-modmenu/src/main/java/com/castleshift/fabric/modmenu/CastleShiftModMenuIntegration.java`
- Create: `fabric/base-modmenu/src/main/resources/fabric.mod.json` (ModMenu entrypoint declaration fragment — see Step 3)
- Modify: `fabric/1.21.1/build.gradle`
- Modify: `fabric/base/src/main/java/com/castleshift/fabric/CastleShiftFabric.java` (client tick keybinding)
- Modify: `neoforge/base/src/main/java/com/castleshift/neoforge/CastleShiftNeoForge.java` (client tick keybinding, NeoForge client bus)
- Modify: `forge/base/src/main/java/com/castleshift/forge/CastleShiftForge.java` (client tick keybinding, Forge client bus)

**Interfaces:**
- Consumes: `ConfigScreen` (Task 5), each loader's own config-dir API (`FabricLoader.getInstance().getConfigDir()`, NeoForge/Forge `FMLPaths.CONFIGDIR.get()`).
- Produces: nothing consumed by later tasks — this is the terminal integration layer.

- [ ] **Step 1: NeoForge config screen factory and keybinding**

Modify `neoforge/base/src/main/java/com/castleshift/neoforge/CastleShiftNeoForge.java`. `RegisterKeyMappingsEvent` fires on the **mod** event bus on both NeoForge and Forge (it is not a separate client-only bus), so it can be listened to directly in the constructor without a `dist.isClient()` guard — the event itself is only fired client-side by the loader:

```java
    public CastleShiftNeoForge(IEventBus modEventBus) {
        PROCESSOR_TYPES.register(modEventBus);
        PLACEMENT_TYPES.register(modEventBus);
        CastleShift.init();

        modEventBus.addListener(CastleShiftNeoForge::onRegisterKeyMappings);

        net.neoforged.fml.ModContainer container =
                net.neoforged.fml.ModLoadingContext.get().getActiveContainer();
        container.registerExtensionPoint(
                net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                (client, parent) -> new com.castleshift.config.client.ConfigScreen(
                        parent, net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("castleshift.toml")));
    }

    private static void onRegisterKeyMappings(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        event.register(com.castleshift.config.client.ClientConfigKeybind.OPEN_CONFIG_SCREEN);
    }
```

If registering `IConfigScreenFactory` unconditionally in the constructor causes a `ClassNotFoundException`/`NoClassDefFoundError` on a dedicated server (client-only classes referenced from common constructor code), move both the `IConfigScreenFactory` registration and the `addListener` call behind a check on `net.neoforged.fml.loading.FMLEnvironment.dist.isClient()` — verify which is needed by running `neoforge:runServer` (or the dedicated-server path) for 1.21.1 in Step 6 below; this class of bug is silent until a dedicated server actually loads the mod.

- [ ] **Step 2: Shared `ClientConfigKeybind` holder (client-only, safe on dedicated server)**

Create `common/1.21.1/src/main/java/com/castleshift/config/client/ClientConfigKeybind.java`:

```java
package com.castleshift.config.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class ClientConfigKeybind {

    public static final KeyMapping OPEN_CONFIG_SCREEN = new KeyMapping(
            "key.castleshift.open_config",
            InputConstants.UNKNOWN.getValue(),
            "key.categories.castleshift");

    private ClientConfigKeybind() {}

    public static void onClientTick(Minecraft minecraft, Path configFile) {
        if (minecraft.screen == null && OPEN_CONFIG_SCREEN.consumeClick()) {
            minecraft.setScreen(new ConfigScreen(null, configFile));
        }
    }
}
```

Add the lang key to both `en_us.json`/`ja_jp.json`:
`"key.categories.castleshift": "Castle Shift"`, `"key.castleshift.open_config": "Open Castle Shift Config"` (en); `"key.categories.castleshift": "Castle Shift"`, `"key.castleshift.open_config": "Castle Shift設定を開く"` (ja).

This class must only ever be referenced from client-only code paths (Fabric client entrypoint, NeoForge/Forge client-side event listeners) — never from `CastleShift.init()` or any code that also runs on a dedicated server, matching MinersMarket's nested-holder convention.

- [ ] **Step 3: Fabric ModMenu optional module**

Create `fabric/base-modmenu/src/main/java/com/castleshift/fabric/modmenu/CastleShiftModMenuIntegration.java`:

```java
package com.castleshift.fabric.modmenu;

import com.castleshift.config.client.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class CastleShiftModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(
                parent, FabricLoader.getInstance().getConfigDir().resolve("castleshift.toml"));
    }
}
```

Add `fabric/1.21.1/build.gradle` wiring, inside the existing `sourceSets` block:

```groovy
sourceSets {
    main {
        java {
            srcDir '../base-modmenu/src/main/java'
        }
    }
}
```

Add to `dependencies { ... }`:

```groovy
    modCompileOnly "com.terraformersmc:modmenu:${modmenu_version}"
```

Add `modmenu_version=...` to `gradle.properties` (pick the ModMenu release compatible with 1.21.1; check https://modrinth.com/mod/modmenu/versions for the exact string during implementation — record the chosen value in the commit message).

Register the entrypoint in `fabric/1.21.1/src/main/resources/fabric.mod.json` (the version-specific one, not `base`'s), under `entrypoints`:

```json
  "entrypoints": {
    "modmenu": [
      "com.castleshift.fabric.modmenu.CastleShiftModMenuIntegration"
    ]
  }
```

(If `fabric.mod.json` for 1.21.1 doesn't already have an `entrypoints` key, check the existing file first and merge rather than overwrite the `main`/`client` entrypoints already declared there.)

- [ ] **Step 4: Fabric client-tick keybinding registration**

Modify `fabric/base/src/main/java/com/castleshift/fabric/CastleShiftFabric.java` — keybinding registration needs `ClientModInitializer`, so add a sibling client entrypoint class (check whether one already exists under `fabric/base` first; if so, add to it instead of creating a new one):

```java
package com.castleshift.fabric;

import com.castleshift.config.client.ClientConfigKeybind;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;

public class CastleShiftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ClientConfigKeybind.OPEN_CONFIG_SCREEN);
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> ClientConfigKeybind.onClientTick(
                minecraft, FabricLoader.getInstance().getConfigDir().resolve("castleshift.toml")));
    }
}
```

Register it as a `client` entrypoint in `fabric/1.21.1/src/main/resources/fabric.mod.json` if not already wired (check first — a client entrypoint class may already exist for another feature; add to that one instead of creating a duplicate).

- [ ] **Step 5: NeoForge/Forge client-tick keybinding registration**

For NeoForge, add a tick listener next to the `onRegisterKeyMappings` listener added in Step 1, on the same mod event bus (`net.neoforged.neoforge.client.event.ClientTickEvent.Post` is fired on the mod bus):

```java
        modEventBus.addListener(CastleShiftNeoForge::onRegisterKeyMappings);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(CastleShiftNeoForge::onClientTick);
    }

    private static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        com.castleshift.config.client.ClientConfigKeybind.onClientTick(
                minecraft, net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("castleshift.toml"));
```

(`ClientTickEvent` is fired on NeoForge's game-instance bus, `NeoForge.EVENT_BUS`, not the mod bus — unlike `RegisterKeyMappingsEvent`. Confirm this split still holds for the exact NeoForge version pinned in `props/1.21.1.properties` by checking the `ClientTickEvent` Javadoc/source in your IDE; if it has moved to the mod bus, register it there instead.)

For Forge, mirror the same shape using Forge's equivalents: `net.minecraftforge.client.event.RegisterKeyMappingsEvent` on the mod bus (`FMLJavaModLoadingContext.get().getModEventBus()`, added next to the `IConfigScreenFactory` registration in `CastleShiftForge`'s constructor via `net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory`), and `net.minecraftforge.event.TickEvent.ClientTickEvent` on the Forge game-instance bus (`net.minecraftforge.common.MinecraftForge.EVENT_BUS`), filtering to `event.phase == TickEvent.Phase.END` since Forge's tick event fires twice per tick:

```java
    private static void onRegisterKeyMappings(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
        event.register(com.castleshift.config.client.ClientConfigKeybind.OPEN_CONFIG_SCREEN);
    }

    private static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        com.castleshift.config.client.ClientConfigKeybind.onClientTick(
                minecraft, net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("castleshift.toml"));
    }
```

Register both Forge listeners in the constructor:

```java
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CastleShiftForge::onRegisterKeyMappings);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(CastleShiftForge.class);
```

(`MinecraftForge.EVENT_BUS.register(Class)` requires `onClientTick` to be `@net.minecraftforge.eventbus.api.SubscribeEvent public static`, unlike NeoForge's `addListener` — adjust the method modifier and add the annotation accordingly.)

- [ ] **Step 6: Build and manual smoke test**

Run: `./gradlew fabric:build neoforge:build forge:build -Ptarget_mc_version=1.21.1`
Expected: BUILD SUCCESSFUL

Then manually (see Task 7 for the full verification checklist): `./gradlew fabric:runClient -Ptarget_mc_version=1.21.1`, confirm the config screen opens via the keybinding and (if ModMenu is present in the dev run classpath) via ModMenu's mods list; confirm `forge:runClient -Ptarget_mc_version=1.21.1` opens the config screen via the mods list Config button (per the documented `neoforge:runClient -Ptarget_mc_version=1.21.1` launch issue in CLAUDE.md, verify NeoForge's config-button/keybinding behavior via a 1.21.2+ target or accept `forge:runClient` as the primary manual-test path for this version bucket).

- [ ] **Step 7: Commit**

```bash
git add neoforge/base/src/main/java/com/castleshift/neoforge/CastleShiftNeoForge.java \
        forge/base/src/main/java/com/castleshift/forge/CastleShiftForge.java \
        fabric/base-modmenu/ \
        fabric/base/src/main/java/com/castleshift/fabric/CastleShiftFabricClient.java \
        fabric/1.21.1/build.gradle fabric/1.21.1/src/main/resources/fabric.mod.json \
        common/1.21.1/src/main/java/com/castleshift/config/client/ClientConfigKeybind.java \
        common/1.21.1/src/main/resources/assets/castleshift/lang/en_us.json \
        common/1.21.1/src/main/resources/assets/castleshift/lang/ja_jp.json \
        gradle.properties
git commit -m "feat(config): wire config screen into NeoForge/Forge config button, optional ModMenu, and a keybinding"
```

---

### Task 7: Manual verification and docs update

**Files:**
- Modify: `docs/future-expansion-plan.md`

**Interfaces:** none (documentation + manual QA task).

- [ ] **Step 1: Update `docs/future-expansion-plan.md`**

In the "Generation Frequency and Compatibility" section, replace the final paragraph ("Before changing the default... maintained consistently across all supported loaders and versions.") with:

```markdown
Before changing the default, publish example datapacks for sparse, default, and frequent generation. This gives players an immediate answer without forcing one world-generation preference on every pack.

The permanent solution is a bundled config file (TOML) plus an in-game settings screen, letting a player or server operator enable/disable castle generation and choose a spacing/separation preset (sparse/default/frequent) with optional numeric overrides that take priority over the preset when set. This follows the vanilla-widget config screen pattern already proven in the sibling MinersMarket project, with no Cloth Config or mandatory ModMenu dependency. A config change takes effect for newly generated chunks without a world restart; already-generated chunks are unaffected, the same as changing spacing in a datapack today.
```

- [ ] **Step 2: Manual verification pass on 1.21.1**

Run `./gradlew forge:runClient -Ptarget_mc_version=1.21.1` (per CLAUDE.md, prefer this over `neoforge:runClient -Ptarget_mc_version=1.21.1` for 1.21.1 interactive testing) and confirm, in a fresh world:

1. Open the config screen via the keybinding; confirm it opens and closes without error.
2. Set `enabled=false`, Done, explore newly generated chunks for several minutes; confirm no new castle spawns.
3. Set `enabled=true`, `preset=FREQUENT`, explore a large radius of fresh chunks; confirm castles appear more often than the DEFAULT baseline (32-chunk spacing).
4. Set a custom spacing (e.g. 200) with no custom separation; confirm the effective spacing visibly changes and separation still behaves sanely (no crash, no obviously broken clustering).
5. Enter an invalid combination (e.g. spacing 10, separation 50) directly in the fields; confirm the field turns red/Done disables per the validation Task 5 implements.
6. Confirm previously generated castles (before the config change) are untouched.
7. Restart the game, confirm the config screen re-opens with the previously saved values (persistence round-trip).

- [ ] **Step 3: Commit**

```bash
git add docs/future-expansion-plan.md
git commit -m "docs: document the castle generation config in the expansion plan"
```

---

## Follow-up (not part of this plan)

- Port this feature to the other supported MC versions/loaders (1.20.1, 1.21.2–1.21.11, 26.1.2–26.3), replicating the per-version `config`/`world/placement` packages the same way the existing `StructureProcessor` classes are duplicated per version — including the 26.2 interface-based `StructurePlacementType`/`MapCodec<? extends StructurePlacement>` split noted in CLAUDE.md.
- Tune the SPARSE/DEFAULT/FREQUENT numeric presets based on playtesting feedback.
- Consider a second config entry once a future large-castle archetype (tracked separately in `docs/future-expansion-plan.md`) is implemented.
