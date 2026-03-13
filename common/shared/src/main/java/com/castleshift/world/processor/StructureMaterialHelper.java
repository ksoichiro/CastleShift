package com.castleshift.world.processor;

/**
 * Extracted logic from StructureStartMixin for determining material combination
 * and weathering state based on world seed and structure position.
 */
public class StructureMaterialHelper {

    /**
     * Derives a deterministic seed from the world seed and structure center position.
     */
    public static long deriveSeed(long worldSeed, int centerX, int centerZ) {
        return worldSeed ^ (centerX * 341873128712L + centerZ * 132897987541L);
    }

    /**
     * Determines the material combination index from a derived seed.
     */
    public static int combinationIndex(long derivedSeed) {
        return (int) (Math.abs(derivedSeed) % MaterialCombination.combinationCount());
    }

    /**
     * Determines whether weathering should be enabled for the given combination and seed.
     * Weathering is only possible when the wall material is STONE_BRICKS or POLISHED_BLACKSTONE_BRICKS.
     */
    public static boolean isWeatheringEnabled(MaterialCombination combination, long derivedSeed) {
        if (combination.wallIndex() == WallMaterialProcessor.STONE_BRICKS
                || combination.wallIndex() == WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS) {
            long weatheringSeed = derivedSeed ^ 0x9E3779B97F4A7C15L;
            return (Math.abs(weatheringSeed) % 2) == 0;
        }
        return false;
    }
}
