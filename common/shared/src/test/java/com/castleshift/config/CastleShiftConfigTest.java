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
