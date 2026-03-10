package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class WallMaterialProcessorTest extends McBootstrapTestBase {

    @AfterEach
    void cleanup() {
        WallMaterialContext.clear();
    }

    private StructureTemplate.StructureBlockInfo makeBlockInfo(BlockState state) {
        return new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, state, null);
    }

    private StructureTemplate.StructureBlockInfo process(StructureTemplate.StructureBlockInfo info) {
        return WallMaterialProcessor.INSTANCE.processBlock(
                null, BlockPos.ZERO, BlockPos.ZERO, info, info, new StructurePlaceSettings());
    }

    @Test
    void defaultIndex_returnsUnchanged() {
        // index 0 (STONE_BRICKS) is the original
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.STONE_BRICKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.STONE_BRICKS, result.state().getBlock());
    }

    @Test
    void deepslateBricksIndex_replacesStoneBricks() {
        WallMaterialContext.set(WallMaterialProcessor.DEEPSLATE_BRICKS);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.STONE_BRICKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.DEEPSLATE_BRICKS, result.state().getBlock());
    }

    @Test
    void polishedBlackstoneBricksIndex_replacesStoneBricks() {
        WallMaterialContext.set(WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.STONE_BRICKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.POLISHED_BLACKSTONE_BRICKS, result.state().getBlock());
    }

    @Test
    void sandstoneIndex_replacesStoneBrickSlab() {
        WallMaterialContext.set(WallMaterialProcessor.SANDSTONE);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.STONE_BRICK_SLAB.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.SANDSTONE_SLAB, result.state().getBlock());
    }

    @Test
    void endStoneBricksIndex_replacesStoneBrickStairs() {
        WallMaterialContext.set(WallMaterialProcessor.END_STONE_BRICKS);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.STONE_BRICK_STAIRS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.END_STONE_BRICK_STAIRS, result.state().getBlock());
    }

    @Test
    void endStoneBricksIndex_replacesStoneBrickWall() {
        WallMaterialContext.set(WallMaterialProcessor.END_STONE_BRICKS);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.STONE_BRICK_WALL.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.END_STONE_BRICK_WALL, result.state().getBlock());
    }

    @Test
    void unmappedBlock_returnsUnchanged() {
        WallMaterialContext.set(WallMaterialProcessor.DEEPSLATE_BRICKS);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.OAK_PLANKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.OAK_PLANKS, result.state().getBlock());
    }

    @Test
    void allMaterialMaps_replaceCorrectly() {
        Block[] expected = {
                Blocks.STONE_BRICKS,              // 0: original
                Blocks.DEEPSLATE_BRICKS,           // 1
                Blocks.POLISHED_BLACKSTONE_BRICKS, // 2
                Blocks.SANDSTONE,                  // 3
                Blocks.END_STONE_BRICKS,           // 4
        };
        for (int i = 0; i < expected.length; i++) {
            WallMaterialContext.set(i);
            StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.STONE_BRICKS.defaultBlockState());
            StructureTemplate.StructureBlockInfo result = process(info);
            assertEquals(expected[i], result.state().getBlock(),
                    "Material index " + i + " should map STONE_BRICKS to " + expected[i]);
            WallMaterialContext.clear();
        }
    }
}
