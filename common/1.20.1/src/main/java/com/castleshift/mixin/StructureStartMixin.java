package com.castleshift.mixin;

import com.castleshift.world.processor.MaterialCombination;
import com.castleshift.world.processor.RoofMaterialContext;
import com.castleshift.world.processor.WallMaterialContext;
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
    }

    @Inject(method = "placeInChunk", at = @At("RETURN"))
    private void castleshift$onPlaceInChunkReturn(
            WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
            RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, CallbackInfo ci) {
        RoofMaterialContext.clear();
        WallMaterialContext.clear();
    }
}
