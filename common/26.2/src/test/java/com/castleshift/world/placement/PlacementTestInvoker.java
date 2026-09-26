package com.castleshift.world.placement;

// Absorbs construction/signature differences of the vanilla StructurePlacement base class,
// mirroring the role of ProcessorTestInvoker for StructureProcessor.
// isPlacementChunk(ChunkGeneratorStructureState, int, int) cannot be called from a unit test
// (the state object needs a full chunk generator), so tests go through the seed-based helper
// that isPlacementChunk itself delegates to.
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
