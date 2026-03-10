package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RoofMaterialProcessorTest extends McBootstrapTestBase {

    @AfterEach
    void cleanup() {
        RoofMaterialContext.clear();
    }

    private StructureTemplate.StructureBlockInfo makeBlockInfo(BlockState state) {
        return new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, state, null);
    }

    private StructureTemplate.StructureBlockInfo process(StructureTemplate.StructureBlockInfo info) {
        return RoofMaterialProcessor.INSTANCE.processBlock(
                null, BlockPos.ZERO, BlockPos.ZERO, info, info, new StructurePlaceSettings());
    }

    @Test
    void defaultIndex_returnsUnchanged() {
        // index 0 (SPRUCE) is the original, should not change
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.SPRUCE_PLANKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.SPRUCE_PLANKS, result.state().getBlock());
    }

    @Test
    void darkOakIndex_replacesSprucePlanks() {
        RoofMaterialContext.set(RoofMaterialProcessor.DARK_OAK);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.SPRUCE_PLANKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.DARK_OAK_PLANKS, result.state().getBlock());
    }

    @Test
    void darkOakIndex_replacesSpruceSlab() {
        RoofMaterialContext.set(RoofMaterialProcessor.DARK_OAK);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.SPRUCE_SLAB.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.DARK_OAK_SLAB, result.state().getBlock());
    }

    @Test
    void darkOakIndex_replacesSpruceFence() {
        RoofMaterialContext.set(RoofMaterialProcessor.DARK_OAK);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.SPRUCE_FENCE.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.DARK_OAK_FENCE, result.state().getBlock());
    }

    @Test
    void deepslateIndex_replacesSprucePlanks() {
        RoofMaterialContext.set(RoofMaterialProcessor.DEEPSLATE_BRICKS);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.SPRUCE_PLANKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.DEEPSLATE_BRICKS, result.state().getBlock());
    }

    @Test
    void crimsonIndex_replacesSprucePlanks() {
        RoofMaterialContext.set(RoofMaterialProcessor.CRIMSON);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.SPRUCE_PLANKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.CRIMSON_PLANKS, result.state().getBlock());
    }

    @Test
    void unmappedBlock_returnsUnchanged() {
        RoofMaterialContext.set(RoofMaterialProcessor.DARK_OAK);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.STONE.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.STONE, result.state().getBlock());
    }

    @Test
    void slabProperties_arePreserved() {
        RoofMaterialContext.set(RoofMaterialProcessor.DARK_OAK);
        BlockState topSlab = Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(topSlab);
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.DARK_OAK_SLAB, result.state().getBlock());
        assertEquals(SlabType.TOP, result.state().getValue(SlabBlock.TYPE));
    }

    @Test
    void allMaterialMaps_replaceCorrectly() {
        Block[] expected = {
                Blocks.SPRUCE_PLANKS,      // 0: original (no replacement)
                Blocks.DARK_OAK_PLANKS,    // 1
                Blocks.DEEPSLATE_BRICKS,   // 2
                Blocks.DARK_PRISMARINE,    // 3
                Blocks.RED_NETHER_BRICKS,  // 4
                Blocks.CRIMSON_PLANKS,     // 5
        };
        for (int i = 0; i < expected.length; i++) {
            RoofMaterialContext.set(i);
            StructureTemplate.StructureBlockInfo info = makeBlockInfo(Blocks.SPRUCE_PLANKS.defaultBlockState());
            StructureTemplate.StructureBlockInfo result = process(info);
            assertEquals(expected[i], result.state().getBlock(),
                    "Material index " + i + " should map SPRUCE_PLANKS to " + expected[i]);
            RoofMaterialContext.clear();
        }
    }

    @Test
    void position_isPreserved() {
        RoofMaterialContext.set(RoofMaterialProcessor.DARK_OAK);
        BlockPos pos = new BlockPos(10, 20, 30);
        StructureTemplate.StructureBlockInfo info =
                new StructureTemplate.StructureBlockInfo(pos, Blocks.SPRUCE_PLANKS.defaultBlockState(), null);
        StructureTemplate.StructureBlockInfo result = RoofMaterialProcessor.INSTANCE.processBlock(
                null, BlockPos.ZERO, BlockPos.ZERO, info, info, new StructurePlaceSettings());
        assertEquals(pos, result.pos());
    }
}
