package com.castleshift.world.placement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.castleshift.config.CastleShiftConfig;
import com.castleshift.config.ConfigDefaults;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConfigurableSpreadStructurePlacementTest {

    private static final int SALT = 194572831;

    // StructurePlacement's static init touches BuiltInRegistries, which requires MC bootstrap.
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetConfig() {
        CastleShiftConfig.set(ConfigDefaults.defaults());
    }

    @Test
    void disabledConfigNeverProducesAPlacementChunk() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(false, CastleShiftConfig.Preset.DEFAULT, null, null)));

        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);

        for (int x = -64; x < 64; x++) {
            for (int z = -64; z < 64; z++) {
                assertFalse(
                        PlacementTestInvoker.isPotentialSpreadChunk(placement, 0L, x, z),
                        "expected no placement chunk when generation is disabled");
            }
        }
    }

    @Test
    void enabledConfigProducesPlacementChunks() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, null, null)));

        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);

        boolean any = false;
        for (int x = -64; x < 64 && !any; x++) {
            for (int z = -64; z < 64 && !any; z++) {
                any = PlacementTestInvoker.isPotentialSpreadChunk(placement, 0L, x, z);
            }
        }
        assertTrue(any, "expected at least one placement chunk when generation is enabled");
    }

    @Test
    void enabledConfigUsesEffectiveSpacingFromPreset() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.FREQUENT, null, null)));

        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);

        assertEquals(ConfigDefaults.FREQUENT_SPACING, placement.spacing());
        assertEquals(ConfigDefaults.FREQUENT_SEPARATION, placement.separation());
        assertNotEquals(ConfigDefaults.DEFAULT_SPACING, placement.spacing());
    }

    @Test
    void customSpacingOverridesPreset() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, 77, null)));

        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);

        assertEquals(77, placement.spacing());
    }

    @Test
    void spacingIsReadLiveSoConfigChangesTakeEffectWithoutRebuildingThePlacement() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, null, null)));

        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);
        assertEquals(ConfigDefaults.DEFAULT_SPACING, placement.spacing());

        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.SPARSE, null, null)));

        assertEquals(ConfigDefaults.SPARSE_SPACING, placement.spacing());
    }
}
