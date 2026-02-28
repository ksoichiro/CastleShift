package com.castleshift.world.processor;

import com.mojang.serialization.Codec;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class WallMaterialProcessor extends StructureProcessor {
    public static final WallMaterialProcessor INSTANCE = new WallMaterialProcessor();
    public static final Codec<WallMaterialProcessor> CODEC = Codec.unit(INSTANCE);

    public static final int STONE_BRICKS = 0;
    public static final int DEEPSLATE_BRICKS = 1;
    public static final int POLISHED_BLACKSTONE_BRICKS = 2;
    public static final int SANDSTONE = 3;
    public static final int END_STONE_BRICKS = 4;

    static final List<Map<Block, Block>> MATERIAL_MAPS = List.of(
            Map.of(), // 0: stone bricks (original)
            Map.of(   // 1: deepslate brick
                    Blocks.STONE_BRICKS, Blocks.DEEPSLATE_BRICKS,
                    Blocks.STONE_BRICK_SLAB, Blocks.DEEPSLATE_BRICK_SLAB,
                    Blocks.STONE_BRICK_STAIRS, Blocks.DEEPSLATE_BRICK_STAIRS,
                    Blocks.STONE_BRICK_WALL, Blocks.DEEPSLATE_BRICK_WALL
            ),
            Map.of(   // 2: polished blackstone brick
                    Blocks.STONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS,
                    Blocks.STONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB,
                    Blocks.STONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                    Blocks.STONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL
            ),
            Map.of(   // 3: sandstone
                    Blocks.STONE_BRICKS, Blocks.SANDSTONE,
                    Blocks.STONE_BRICK_SLAB, Blocks.SANDSTONE_SLAB,
                    Blocks.STONE_BRICK_STAIRS, Blocks.SANDSTONE_STAIRS,
                    Blocks.STONE_BRICK_WALL, Blocks.SANDSTONE_WALL
            ),
            Map.of(   // 4: end stone brick
                    Blocks.STONE_BRICKS, Blocks.END_STONE_BRICKS,
                    Blocks.STONE_BRICK_SLAB, Blocks.END_STONE_BRICK_SLAB,
                    Blocks.STONE_BRICK_STAIRS, Blocks.END_STONE_BRICK_STAIRS,
                    Blocks.STONE_BRICK_WALL, Blocks.END_STONE_BRICK_WALL
            )
    );

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos blockPos,
            BlockPos pos,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo modifiedBlockInfo,
            StructurePlaceSettings settings) {
        int materialIndex = WallMaterialContext.get();
        if (materialIndex <= 0 || materialIndex >= MATERIAL_MAPS.size()) {
            return modifiedBlockInfo;
        }

        Block block = modifiedBlockInfo.state().getBlock();
        Block replacement = MATERIAL_MAPS.get(materialIndex).get(block);
        if (replacement == null) {
            return modifiedBlockInfo;
        }

        BlockState newState = copyBlockState(modifiedBlockInfo.state(), replacement);
        return new StructureTemplate.StructureBlockInfo(modifiedBlockInfo.pos(), newState, modifiedBlockInfo.nbt());
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

    @Override
    protected StructureProcessorType<?> getType() {
        return ModProcessors.WALL_MATERIAL;
    }
}
