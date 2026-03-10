package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class WallWeatheringProcessorTest extends McBootstrapTestBase {

    @AfterEach
    void cleanup() {
        WallWeatheringContext.clear();
    }

    private StructureTemplate.StructureBlockInfo makeBlockInfo(BlockPos pos, BlockState state) {
        return new StructureTemplate.StructureBlockInfo(pos, state, null);
    }

    private StructureTemplate.StructureBlockInfo process(StructureTemplate.StructureBlockInfo info) {
        return WallWeatheringProcessor.INSTANCE.processBlock(
                null, BlockPos.ZERO, BlockPos.ZERO, info, info, new StructurePlaceSettings());
    }

    @Test
    void weatheringDisabled_returnsUnchanged() {
        // Default context is disabled
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(BlockPos.ZERO, Blocks.STONE_BRICKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.STONE_BRICKS, result.state().getBlock());
    }

    @Test
    void weatheringEnabled_unmappedBlock_returnsUnchanged() {
        WallWeatheringContext.set(true);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(BlockPos.ZERO, Blocks.OAK_PLANKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(Blocks.OAK_PLANKS, result.state().getBlock());
    }

    @Test
    void weatheringEnabled_stoneBricks_canBeWeathered() {
        WallWeatheringContext.set(true);
        Set<Block> results = new HashSet<>();
        // Try many positions to trigger weathering (10% chance per position)
        for (int x = 0; x < 1000; x++) {
            BlockPos pos = new BlockPos(x, 0, 0);
            StructureTemplate.StructureBlockInfo info = makeBlockInfo(pos, Blocks.STONE_BRICKS.defaultBlockState());
            StructureTemplate.StructureBlockInfo result = process(info);
            results.add(result.state().getBlock());
        }
        // Should have original + at least one weathered variant
        assertTrue(results.contains(Blocks.STONE_BRICKS), "Some blocks should remain unchanged");
        assertTrue(results.contains(Blocks.COBBLESTONE) || results.contains(Blocks.ANDESITE),
                "Some blocks should be weathered to cobblestone or andesite");
    }

    @Test
    void weatheringEnabled_polishedBlackstoneBricks_canBeWeathered() {
        WallWeatheringContext.set(true);
        Set<Block> results = new HashSet<>();
        for (int x = 0; x < 1000; x++) {
            BlockPos pos = new BlockPos(x, 0, 0);
            StructureTemplate.StructureBlockInfo info = makeBlockInfo(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            StructureTemplate.StructureBlockInfo result = process(info);
            results.add(result.state().getBlock());
        }
        assertTrue(results.contains(Blocks.POLISHED_BLACKSTONE_BRICKS), "Some blocks should remain unchanged");
        assertTrue(results.contains(Blocks.BLACKSTONE) || results.contains(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS),
                "Some blocks should be weathered to blackstone or cracked polished blackstone bricks");
    }

    @Test
    void weathering_isDeterministic() {
        WallWeatheringContext.set(true);
        BlockPos pos = new BlockPos(42, 10, 99);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(pos, Blocks.STONE_BRICKS.defaultBlockState());
        Block result1 = process(info).state().getBlock();
        Block result2 = process(info).state().getBlock();
        assertEquals(result1, result2, "Same position should always produce the same result");
    }

    @Test
    void position_isPreserved() {
        WallWeatheringContext.set(true);
        BlockPos pos = new BlockPos(5, 10, 15);
        StructureTemplate.StructureBlockInfo info = makeBlockInfo(pos, Blocks.STONE_BRICKS.defaultBlockState());
        StructureTemplate.StructureBlockInfo result = process(info);
        assertEquals(pos, result.pos());
    }
}
