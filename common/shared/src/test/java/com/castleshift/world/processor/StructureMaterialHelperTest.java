package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class StructureMaterialHelperTest {

    @Test
    void sameSeedAndCoordinatesProduceSameIndex() {
        long seed = StructureMaterialHelper.deriveSeed(12345L, 100, 200);
        int index1 = StructureMaterialHelper.combinationIndex(seed);
        int index2 = StructureMaterialHelper.combinationIndex(seed);
        assertEquals(index1, index2);
    }

    @Test
    void deriveSeedIsDeterministic() {
        long seed1 = StructureMaterialHelper.deriveSeed(12345L, 100, 200);
        long seed2 = StructureMaterialHelper.deriveSeed(12345L, 100, 200);
        assertEquals(seed1, seed2);
    }

    @Test
    void deriveSeedDiffersForDifferentPositions() {
        long seed1 = StructureMaterialHelper.deriveSeed(12345L, 100, 200);
        long seed2 = StructureMaterialHelper.deriveSeed(12345L, 200, 100);
        assertTrue(seed1 != seed2, "Different positions should produce different seeds");
    }

    @Test
    void allIndicesAreWithinValidRange() {
        int count = MaterialCombination.combinationCount();
        for (int x = -500; x <= 500; x += 50) {
            for (int z = -500; z <= 500; z += 50) {
                long seed = StructureMaterialHelper.deriveSeed(98765L, x, z);
                int index = StructureMaterialHelper.combinationIndex(seed);
                assertTrue(index >= 0 && index < count,
                        "index " + index + " out of range for coords (" + x + ", " + z + ")");
            }
        }
    }

    @Test
    void allCombinationIndicesAppearInSamples() {
        int count = MaterialCombination.combinationCount();
        Set<Integer> seen = new HashSet<>();
        for (long worldSeed = 0; worldSeed < 10; worldSeed++) {
            for (int x = -1000; x <= 1000; x += 16) {
                for (int z = -1000; z <= 1000; z += 16) {
                    long seed = StructureMaterialHelper.deriveSeed(worldSeed, x, z);
                    seen.add(StructureMaterialHelper.combinationIndex(seed));
                    if (seen.size() == count) {
                        return;
                    }
                }
            }
        }
        assertEquals(count, seen.size(), "Not all combination indices appeared");
    }

    @Test
    void weatheringOnlyForEligibleWalls() {
        for (int x = -500; x <= 500; x += 50) {
            for (int z = -500; z <= 500; z += 50) {
                long seed = StructureMaterialHelper.deriveSeed(42L, x, z);
                int index = StructureMaterialHelper.combinationIndex(seed);
                MaterialCombination c = MaterialCombination.get(index);
                boolean weathering = StructureMaterialHelper.isWeatheringEnabled(c, seed);
                if (c.wallIndex() != WallMaterialProcessor.STONE_BRICKS
                        && c.wallIndex() != WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS) {
                    assertFalse(weathering,
                            "weathering should be false for wallIndex " + c.wallIndex()
                                    + " at (" + x + ", " + z + ")");
                }
            }
        }
    }

    @Test
    void weatheringIsDeterministic() {
        long seed = StructureMaterialHelper.deriveSeed(777L, 128, 256);
        int index = StructureMaterialHelper.combinationIndex(seed);
        MaterialCombination c = MaterialCombination.get(index);
        boolean w1 = StructureMaterialHelper.isWeatheringEnabled(c, seed);
        boolean w2 = StructureMaterialHelper.isWeatheringEnabled(c, seed);
        assertEquals(w1, w2);
    }

    @Test
    void weatheringEnabledForStoneBricks() {
        // Find a combination with STONE_BRICKS wall
        MaterialCombination stoneBrickCombo = null;
        for (int i = 0; i < MaterialCombination.combinationCount(); i++) {
            MaterialCombination c = MaterialCombination.get(i);
            if (c.wallIndex() == WallMaterialProcessor.STONE_BRICKS) {
                stoneBrickCombo = c;
                break;
            }
        }
        assertTrue(stoneBrickCombo != null, "Should have a STONE_BRICKS combination");

        // Weathering result depends on seed, but should not always be false
        boolean anyEnabled = false;
        for (long s = 0; s < 100; s++) {
            if (StructureMaterialHelper.isWeatheringEnabled(stoneBrickCombo, s)) {
                anyEnabled = true;
                break;
            }
        }
        assertTrue(anyEnabled, "Weathering should be enabled for some seeds with STONE_BRICKS wall");
    }

    @Test
    void weatheringEnabledForPolishedBlackstoneBricks() {
        MaterialCombination blackstoneCombo = null;
        for (int i = 0; i < MaterialCombination.combinationCount(); i++) {
            MaterialCombination c = MaterialCombination.get(i);
            if (c.wallIndex() == WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS) {
                blackstoneCombo = c;
                break;
            }
        }
        assertTrue(blackstoneCombo != null, "Should have a POLISHED_BLACKSTONE_BRICKS combination");

        boolean anyEnabled = false;
        for (long s = 0; s < 100; s++) {
            if (StructureMaterialHelper.isWeatheringEnabled(blackstoneCombo, s)) {
                anyEnabled = true;
                break;
            }
        }
        assertTrue(anyEnabled, "Weathering should be enabled for some seeds with POLISHED_BLACKSTONE_BRICKS wall");
    }

    @Test
    void weatheringNeverEnabledForOtherWalls() {
        // Test all wall types that should never have weathering
        int[] nonWeatheringWalls = {
                WallMaterialProcessor.DEEPSLATE_BRICKS,
                WallMaterialProcessor.SANDSTONE,
                WallMaterialProcessor.END_STONE_BRICKS
        };

        for (int wallIndex : nonWeatheringWalls) {
            MaterialCombination combo = null;
            for (int i = 0; i < MaterialCombination.combinationCount(); i++) {
                MaterialCombination c = MaterialCombination.get(i);
                if (c.wallIndex() == wallIndex) {
                    combo = c;
                    break;
                }
            }
            if (combo == null) continue;

            for (long s = 0; s < 100; s++) {
                assertFalse(StructureMaterialHelper.isWeatheringEnabled(combo, s),
                        "Weathering should never be enabled for wallIndex " + wallIndex);
            }
        }
    }
}
