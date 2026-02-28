package com.castleshift.mixin;

import com.castleshift.world.processor.MaterialCombination;
import com.castleshift.world.processor.RoofMaterialContext;
import com.castleshift.world.processor.StairMaterialContext;
import com.castleshift.world.processor.WallMaterialContext;
import com.castleshift.world.processor.WallWeatheringContext;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public class StructureStartMixin {
    @Inject(method = "placeInChunk", at = @At("HEAD"))
    private void castleshift$onPlaceInChunkHead(
            WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
            RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, CallbackInfo ci) {
        StructureStart self = (StructureStart) (Object) this;
        net.minecraft.core.BlockPos center = self.getBoundingBox().getCenter();
        long seed = level.getSeed() ^ (center.getX() * 341873128712L + center.getZ() * 132897987541L);
        int combinationIndex = (int) (Math.abs(seed) % MaterialCombination.combinationCount());
        MaterialCombination combination = MaterialCombination.get(combinationIndex);
        RoofMaterialContext.set(combination.roofIndex());
        WallMaterialContext.set(combination.wallIndex());
        StairMaterialContext.set(combination.stairIndex());
        // Weathering applies to stone brick walls (0) and polished blackstone brick walls (2)
        if (combination.wallIndex() == 0 || combination.wallIndex() == 2) {
            long weatheringSeed = seed ^ 0x9E3779B97F4A7C15L;
            boolean weatheringEnabled = (Math.abs(weatheringSeed) % 2) == 0;
            WallWeatheringContext.set(weatheringEnabled);
        } else {
            WallWeatheringContext.set(false);
        }
    }

    @Inject(method = "placeInChunk", at = @At("RETURN"))
    private void castleshift$onPlaceInChunkReturn(
            WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
            RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, CallbackInfo ci) {
        RoofMaterialContext.clear();
        WallMaterialContext.clear();
        StairMaterialContext.clear();
        WallWeatheringContext.clear();
    }
}
