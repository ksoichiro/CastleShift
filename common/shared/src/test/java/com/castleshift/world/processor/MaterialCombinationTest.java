package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MaterialCombinationTest {

    @Test
    void combinationCountMatchesAllowedCombinationsSize() {
        assertEquals(MaterialCombination.ALLOWED_COMBINATIONS.size(), MaterialCombination.combinationCount());
    }

    @Test
    void getReturnsNonNullForAllValidIndices() {
        int count = MaterialCombination.combinationCount();
        for (int i = 0; i < count; i++) {
            assertNotNull(MaterialCombination.get(i), "get(" + i + ") should not be null");
        }
    }

    @Test
    void getThrowsForNegativeIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> MaterialCombination.get(-1));
    }

    @Test
    void getThrowsForIndexEqualToCount() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> MaterialCombination.get(MaterialCombination.combinationCount()));
    }

    @Test
    void allRoofIndicesAreInValidRange() {
        // Max roof index is RoofMaterialProcessor.CRIMSON (5)
        int maxRoof = RoofMaterialProcessor.CRIMSON;
        for (int i = 0; i < MaterialCombination.combinationCount(); i++) {
            MaterialCombination c = MaterialCombination.get(i);
            assertTrue(c.roofIndex() >= 0 && c.roofIndex() <= maxRoof,
                    "roofIndex " + c.roofIndex() + " out of range at index " + i);
        }
    }

    @Test
    void allWallIndicesAreInValidRange() {
        // Max wall index is WallMaterialProcessor.END_STONE_BRICKS (4)
        int maxWall = WallMaterialProcessor.END_STONE_BRICKS;
        for (int i = 0; i < MaterialCombination.combinationCount(); i++) {
            MaterialCombination c = MaterialCombination.get(i);
            assertTrue(c.wallIndex() >= 0 && c.wallIndex() <= maxWall,
                    "wallIndex " + c.wallIndex() + " out of range at index " + i);
        }
    }

    @Test
    void allStairIndicesAreInValidRange() {
        // Max stair index is StairMaterialProcessor.RED_NETHER_BRICKS (6)
        int maxStair = StairMaterialProcessor.RED_NETHER_BRICKS;
        for (int i = 0; i < MaterialCombination.combinationCount(); i++) {
            MaterialCombination c = MaterialCombination.get(i);
            assertTrue(c.stairIndex() >= 0 && c.stairIndex() <= maxStair,
                    "stairIndex " + c.stairIndex() + " out of range at index " + i);
        }
    }
}
