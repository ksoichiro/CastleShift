package com.castleshift.world.processor;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

// MC 26.x: StructureProcessor is now an interface; processBlock signature changed
public class RoofMaterialProcessor implements StructureProcessor {
    public static final RoofMaterialProcessor INSTANCE = new RoofMaterialProcessor();
    public static final MapCodec<RoofMaterialProcessor> CODEC = MapCodec.unit(INSTANCE);

    public static final int SPRUCE = 0;
    public static final int DARK_OAK = 1;
    public static final int DEEPSLATE_BRICKS = 2;
    public static final int DARK_PRISMARINE = 3;
    public static final int RED_NETHER_BRICKS = 4;
    public static final int CRIMSON = 5;

    static final List<Map<Block, Block>> MATERIAL_MAPS = List.of(
            Map.of(), // 0: spruce (original)
            Map.of(   // 1: dark oak
                    Blocks.SPRUCE_PLANKS, Blocks.DARK_OAK_PLANKS,
                    Blocks.SPRUCE_SLAB, Blocks.DARK_OAK_SLAB,
                    Blocks.SPRUCE_STAIRS, Blocks.DARK_OAK_STAIRS,
                    Blocks.SPRUCE_FENCE, Blocks.DARK_OAK_FENCE
            ),
            Map.of(   // 2: deepslate bricks
                    Blocks.SPRUCE_PLANKS, Blocks.DEEPSLATE_BRICKS,
                    Blocks.SPRUCE_SLAB, Blocks.DEEPSLATE_BRICK_SLAB,
                    Blocks.SPRUCE_STAIRS, Blocks.DEEPSLATE_BRICK_STAIRS,
                    Blocks.SPRUCE_FENCE, Blocks.DEEPSLATE_BRICK_WALL
            ),
            Map.of(   // 3: dark prismarine
                    Blocks.SPRUCE_PLANKS, Blocks.DARK_PRISMARINE,
                    Blocks.SPRUCE_SLAB, Blocks.DARK_PRISMARINE_SLAB,
                    Blocks.SPRUCE_STAIRS, Blocks.DARK_PRISMARINE_STAIRS,
                    Blocks.SPRUCE_FENCE, Blocks.PRISMARINE_WALL
            ),
            Map.of(   // 4: red nether bricks
                    Blocks.SPRUCE_PLANKS, Blocks.RED_NETHER_BRICKS,
                    Blocks.SPRUCE_SLAB, Blocks.RED_NETHER_BRICK_SLAB,
                    Blocks.SPRUCE_STAIRS, Blocks.RED_NETHER_BRICK_STAIRS,
                    Blocks.SPRUCE_FENCE, Blocks.RED_NETHER_BRICK_WALL
            ),
            Map.of(   // 5: crimson
                    Blocks.SPRUCE_PLANKS, Blocks.CRIMSON_PLANKS,
                    Blocks.SPRUCE_SLAB, Blocks.CRIMSON_SLAB,
                    Blocks.SPRUCE_STAIRS, Blocks.CRIMSON_STAIRS,
                    Blocks.SPRUCE_FENCE, Blocks.CRIMSON_FENCE
            )
    );

    @Override
    public MapCodec<? extends StructureProcessor> codec() {
        return CODEC;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos blockPos,
            BlockPos pos,
            BlockPos offset,
            StructureTemplate.StructureBlockInfo blockInfo,
            StructurePlaceSettings settings) {
        int materialIndex = RoofMaterialContext.get();
        if (materialIndex <= 0 || materialIndex >= MATERIAL_MAPS.size()) {
            return blockInfo;
        }

        Block block = blockInfo.state().getBlock();
        Block replacement = MATERIAL_MAPS.get(materialIndex).get(block);
        if (replacement == null) {
            return blockInfo;
        }

        BlockState newState = copyBlockState(blockInfo.state(), replacement);
        return new StructureTemplate.StructureBlockInfo(blockInfo.pos(), newState, blockInfo.nbt());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState copyBlockState(BlockState source, Block targetBlock) {
        BlockState newState = targetBlock.defaultBlockState();
        for (Property property : source.getProperties()) {
            if (newState.hasProperty(property)) {
                newState = newState.setValue(property, source.getValue(property));
            }
        }
        return newState;
    }
}
