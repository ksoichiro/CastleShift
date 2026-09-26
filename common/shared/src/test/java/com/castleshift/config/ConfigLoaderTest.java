package com.castleshift.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void clampsPresetSeparationBelowCustomSpacing() throws IOException {
        // Only spacing is overridden: the separation still comes from the SPARSE preset (16),
        // which would otherwise be >= the custom spacing of 8.
        Path configFile = tempDir.resolve("castleshift.toml");
        Files.writeString(
                configFile,
                """
                schema_version = 1
                [generation]
                    enabled = true
                    preset = "SPARSE"
                    custom_spacing = 8
                """);

        CastleShiftConfig.Generation loaded = ConfigLoader.load(configFile).generation();

        assertEquals(8, loaded.effectiveSpacing());
        assertTrue(
                loaded.effectiveSeparation() < 8,
                "effective separation must stay below the effective spacing, was "
                        + loaded.effectiveSeparation());
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
