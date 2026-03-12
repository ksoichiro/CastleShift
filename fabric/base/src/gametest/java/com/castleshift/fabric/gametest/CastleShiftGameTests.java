package com.castleshift.fabric.gametest;

import com.castleshift.world.processor.RoofMaterialContext;
import com.castleshift.world.processor.RoofMaterialProcessor;
import com.castleshift.world.processor.StairMaterialContext;
import com.castleshift.world.processor.StairMaterialProcessor;
import com.castleshift.world.processor.WallMaterialContext;
import com.castleshift.world.processor.WallMaterialProcessor;
import com.castleshift.world.processor.WallWeatheringContext;
import com.castleshift.world.processor.WallWeatheringProcessor;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashSet;
import java.util.Set;

public class CastleShiftGameTests implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void processorsRegistered(GameTestHelper helper) {
        String[] processorIds = {
                "castleshift:roof_material",
                "castleshift:wall_material",
                "castleshift:stair_material",
                "castleshift:wall_weathering"
        };

        for (String id : processorIds) {
            ResourceLocation resourceLocation = ResourceLocation.parse(id);
            if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(resourceLocation)) {
                helper.fail("Processor not registered: " + id);
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void roofMaterialProcessor(GameTestHelper helper) {
        try {
            RoofMaterialContext.set(RoofMaterialProcessor.DARK_OAK);

            BlockPos testPos = new BlockPos(0, 1, 0);
            StructureTemplate.StructureBlockInfo input = new StructureTemplate.StructureBlockInfo(
                    testPos, Blocks.SPRUCE_PLANKS.defaultBlockState(), null);
            StructurePlaceSettings settings = new StructurePlaceSettings();

            StructureTemplate.StructureBlockInfo result = RoofMaterialProcessor.INSTANCE.processBlock(
                    helper.getLevel(), testPos, testPos, input, input, settings);

            Block resultBlock = result.state().getBlock();
            if (resultBlock != Blocks.DARK_OAK_PLANKS) {
                helper.fail("Expected DARK_OAK_PLANKS but got " + resultBlock);
                return;
            }

            helper.succeed();
        } finally {
            RoofMaterialContext.clear();
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void wallMaterialProcessor(GameTestHelper helper) {
        try {
            WallMaterialContext.set(WallMaterialProcessor.DEEPSLATE_BRICKS);

            BlockPos testPos = new BlockPos(0, 1, 0);
            StructureTemplate.StructureBlockInfo input = new StructureTemplate.StructureBlockInfo(
                    testPos, Blocks.STONE_BRICKS.defaultBlockState(), null);
            StructurePlaceSettings settings = new StructurePlaceSettings();

            StructureTemplate.StructureBlockInfo result = WallMaterialProcessor.INSTANCE.processBlock(
                    helper.getLevel(), testPos, testPos, input, input, settings);

            Block resultBlock = result.state().getBlock();
            if (resultBlock != Blocks.DEEPSLATE_BRICKS) {
                helper.fail("Expected DEEPSLATE_BRICKS but got " + resultBlock);
                return;
            }

            helper.succeed();
        } finally {
            WallMaterialContext.clear();
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void stairMaterialProcessor(GameTestHelper helper) {
        try {
            StairMaterialContext.set(StairMaterialProcessor.STONE_BRICKS);

            BlockPos testPos = new BlockPos(0, 1, 0);
            StructureTemplate.StructureBlockInfo input = new StructureTemplate.StructureBlockInfo(
                    testPos, Blocks.BRICKS.defaultBlockState(), null);
            StructurePlaceSettings settings = new StructurePlaceSettings();

            StructureTemplate.StructureBlockInfo result = StairMaterialProcessor.INSTANCE.processBlock(
                    helper.getLevel(), testPos, testPos, input, input, settings);

            Block resultBlock = result.state().getBlock();
            if (resultBlock != Blocks.STONE_BRICKS) {
                helper.fail("Expected STONE_BRICKS but got " + resultBlock);
                return;
            }

            helper.succeed();
        } finally {
            StairMaterialContext.clear();
        }
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 200)
    public void wallWeatheringProcessor(GameTestHelper helper) {
        try {
            WallWeatheringContext.set(true);

            Set<Block> weatheredBlocks = new HashSet<>();
            StructurePlaceSettings settings = new StructurePlaceSettings();

            for (int i = 0; i < 200; i++) {
                BlockPos testPos = new BlockPos(i % 16, 1, i / 16);
                StructureTemplate.StructureBlockInfo input = new StructureTemplate.StructureBlockInfo(
                        testPos, Blocks.STONE_BRICKS.defaultBlockState(), null);

                StructureTemplate.StructureBlockInfo result = WallWeatheringProcessor.INSTANCE.processBlock(
                        helper.getLevel(), testPos, testPos, input, input, settings);

                Block resultBlock = result.state().getBlock();
                if (resultBlock != Blocks.STONE_BRICKS) {
                    weatheredBlocks.add(resultBlock);
                }
            }

            if (weatheredBlocks.isEmpty()) {
                helper.fail("No weathering occurred in 200 blocks");
                return;
            }

            Set<Block> expectedWeathered = Set.of(Blocks.COBBLESTONE, Blocks.ANDESITE);
            for (Block block : weatheredBlocks) {
                if (!expectedWeathered.contains(block)) {
                    helper.fail("Unexpected weathered block: " + block);
                    return;
                }
            }

            helper.succeed();
        } finally {
            WallWeatheringContext.clear();
        }
    }
}
