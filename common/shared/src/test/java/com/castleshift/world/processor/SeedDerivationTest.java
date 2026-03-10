package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SeedDerivationTest {

    /**
     * Reproduces the seed derivation logic from StructureStartMixin
     * without any Minecraft dependencies.
     */
    private static int deriveCombinationIndex(long worldSeed, int centerX, int centerZ) {
        long seed = worldSeed ^ (centerX * 341873128712L + centerZ * 132897987541L);
        return (int) (Math.abs(seed) % MaterialCombination.combinationCount());
    }

    private static boolean deriveWeatheringEnabled(long worldSeed, int centerX, int centerZ) {
        long seed = worldSeed ^ (centerX * 341873128712L + centerZ * 132897987541L);
        int combinationIndex = (int) (Math.abs(seed) % MaterialCombination.combinationCount());
        MaterialCombination combination = MaterialCombination.get(combinationIndex);
        if (combination.wallIndex() == WallMaterialProcessor.STONE_BRICKS
                || combination.wallIndex() == WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS) {
            long weatheringSeed = seed ^ 0x9E3779B97F4A7C15L;
            return (Math.abs(weatheringSeed) % 2) == 0;
        }
        return false;
    }

    @Test
    void sameSeedAndCoordinatesProduceSameIndex() {
        long seed = 12345L;
        int x = 100;
        int z = 200;
        int index1 = deriveCombinationIndex(seed, x, z);
        int index2 = deriveCombinationIndex(seed, x, z);
        assertEquals(index1, index2);
    }

    @Test
    void allIndicesAreWithinValidRange() {
        long seed = 98765L;
        int count = MaterialCombination.combinationCount();
        for (int x = -500; x <= 500; x += 50) {
            for (int z = -500; z <= 500; z += 50) {
                int index = deriveCombinationIndex(seed, x, z);
                assertTrue(index >= 0 && index < count,
                        "index " + index + " out of range for coords (" + x + ", " + z + ")");
            }
        }
    }

    @Test
    void allCombinationIndicesAppearInSamples() {
        int count = MaterialCombination.combinationCount();
        Set<Integer> seen = new HashSet<>();
        // Use many coordinates to cover all indices
        for (long seed = 0; seed < 10; seed++) {
            for (int x = -1000; x <= 1000; x += 16) {
                for (int z = -1000; z <= 1000; z += 16) {
                    seen.add(deriveCombinationIndex(seed, x, z));
                    if (seen.size() == count) {
                        return; // All indices covered
                    }
                }
            }
        }
        assertEquals(count, seen.size(), "Not all combination indices appeared. Missing: " + missingIndices(seen, count));
    }

    @Test
    void weatheringOnlyForEligibleWalls() {
        long seed = 42L;
        for (int x = -500; x <= 500; x += 50) {
            for (int z = -500; z <= 500; z += 50) {
                int index = deriveCombinationIndex(seed, x, z);
                MaterialCombination c = MaterialCombination.get(index);
                boolean weathering = deriveWeatheringEnabled(seed, x, z);
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
        long seed = 777L;
        int x = 128;
        int z = 256;
        boolean w1 = deriveWeatheringEnabled(seed, x, z);
        boolean w2 = deriveWeatheringEnabled(seed, x, z);
        assertEquals(w1, w2);
    }

    private static String missingIndices(Set<Integer> seen, int count) {
        Set<Integer> missing = new HashSet<>();
        for (int i = 0; i < count; i++) {
            if (!seen.contains(i)) {
                missing.add(i);
            }
        }
        return missing.toString();
    }
}
