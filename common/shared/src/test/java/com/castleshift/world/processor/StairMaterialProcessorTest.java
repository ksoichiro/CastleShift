package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class StairMaterialProcessorTest extends McBootstrapTestBase {

    @AfterEach
    void cleanup() {
        StairMaterialContext.clear();
    }

    private StructureTemplate.StructureBlockInfo makeBlockInfo(BlockState state) {
        return new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, state, null);
    }

    private StructureTemplate.StructureBlockInfo process(StructureTemplate.StructureBlockInfo info) {
        return StairMaterialProcessor.INSTANCE.processBlock(
                null, BlockPos.ZERO, BlockPos.ZERO, info, info, new StructurePlaceSettings());
    }

    @Test
    void defaultIndex_returnsUnchanged() {
        // index 0 (BRICKS) is the original
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.BRICKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.BRICKS, result.state().getBlock());
    }

    @Test
    void stoneBricksIndex_replacesBricks() {
        StairMaterialContext.set(StairMaterialProcessor.STONE_BRICKS);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.BRICKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.STONE_BRICKS, result.state().getBlock());
    }

    @Test
    void polishedGraniteIndex_replacesBrickStairs() {
        StairMaterialContext.set(StairMaterialProcessor.POLISHED_GRANITE);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.BRICK_STAIRS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.POLISHED_GRANITE_STAIRS, result.state().getBlock());
    }

    @Test
    void deepslateBricksIndex_replacesBricks() {
        StairMaterialContext.set(StairMaterialProcessor.DEEPSLATE_BRICKS);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.BRICKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.DEEPSLATE_BRICKS, result.state().getBlock());
    }

    @Test
    void redNetherBricksIndex_replacesBrickStairs() {
        StairMaterialContext.set(StairMaterialProcessor.RED_NETHER_BRICKS);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.BRICK_STAIRS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.RED_NETHER_BRICK_STAIRS, result.state().getBlock());
    }

    @Test
    void stairProperties_arePreserved() {
        StairMaterialContext.set(StairMaterialProcessor.STONE_BRICKS);
        BlockState topStair = Blocks.BRICK_STAIRS.defaultBlockState().setValue(StairBlock.HALF, Half.TOP);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(topStair);
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.STONE_BRICK_STAIRS, result.state().getBlock());
        assertEquals(Half.TOP, result.state().getValue(StairBlock.HALF));
    }

    @Test
    void unmappedBlock_returnsUnchanged() {
        StairMaterialContext.set(StairMaterialProcessor.STONE_BRICKS);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.COBBLESTONE.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.COBBLESTONE, result.state().getBlock());
    }

    @Test
    void allMaterialMaps_replaceCorrectly() {
        Block[] expected = {
                Blocks.BRICKS,                      // 0: original
                Blocks.STONE_BRICKS,                 // 1
                Blocks.POLISHED_GRANITE,             // 2
                Blocks.DEEPSLATE_BRICKS,             // 3
                Blocks.POLISHED_BLACKSTONE,          // 4
                Blocks.POLISHED_BLACKSTONE_BRICKS,   // 5
                Blocks.RED_NETHER_BRICKS,            // 6
        };
        for (int i = 0; i < expected.length; i++) {
            StairMaterialContext.set(i);
            StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.BRICKS.defaultBlockState());
            StructureTemplate.StructureBlockInfo result = process(info);
            assertEquals(expected[i], result.state().getBlock(),
                    "Material index " + i + " should map BRICKS to " + expected[i]);
            StairMaterialContext.clear();
        }
    }
}
