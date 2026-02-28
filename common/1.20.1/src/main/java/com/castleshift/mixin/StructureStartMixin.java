package com.castleshift.mixin;

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
    private static final int ROOF_MATERIAL_COUNT = 6;
    private static final int WALL_MATERIAL_COUNT = 5;

    @Inject(method = "placeInChunk", at = @At("HEAD"))
    private void castleshift$onPlaceInChunkHead(
            WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
            RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, CallbackInfo ci) {
        StructureStart self = (StructureStart) (Object) this;
        net.minecraft.core.BlockPos center = self.getBoundingBox().getCenter();
        long roofSeed = level.getSeed() ^ (center.getX() * 341873128712L + center.getZ() * 132897987541L);
        int roofMaterialIndex = (int) (Math.abs(roofSeed) % ROOF_MATERIAL_COUNT);
        RoofMaterialContext.set(roofMaterialIndex);

        long wallSeed = level.getSeed() ^ (center.getX() * 6364136223846793005L + center.getZ() * 1442695040888963407L);
        int wallMaterialIndex = (int) (Math.abs(wallSeed) % WALL_MATERIAL_COUNT);
        WallMaterialContext.set(wallMaterialIndex);
    }

    @Inject(method = "placeInChunk", at = @At("RETURN"))
    private void castleshift$onPlaceInChunkReturn(
            WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
            RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, CallbackInfo ci) {
        RoofMaterialContext.clear();
        WallMaterialContext.clear();
    }
}
