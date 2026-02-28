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

public class StairMaterialProcessor extends StructureProcessor {
    public static final StairMaterialProcessor INSTANCE = new StairMaterialProcessor();
    public static final Codec<StairMaterialProcessor> CODEC = Codec.unit(INSTANCE);

    static final List<Map<Block, Block>> MATERIAL_MAPS = List.of(
            Map.of(), // 0: bricks (original)
            Map.of(   // 1: stone bricks
                    Blocks.BRICKS, Blocks.STONE_BRICKS,
                    Blocks.BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS
            ),
            Map.of(   // 2: polished granite
                    Blocks.BRICKS, Blocks.POLISHED_GRANITE,
                    Blocks.BRICK_STAIRS, Blocks.POLISHED_GRANITE_STAIRS
            ),
            Map.of(   // 3: deepslate brick
                    Blocks.BRICKS, Blocks.DEEPSLATE_BRICKS,
                    Blocks.BRICK_STAIRS, Blocks.DEEPSLATE_BRICK_STAIRS
            ),
            Map.of(   // 4: polished blackstone
                    Blocks.BRICKS, Blocks.POLISHED_BLACKSTONE,
                    Blocks.BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_STAIRS
            ),
            Map.of(   // 5: polished blackstone brick
                    Blocks.BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS,
                    Blocks.BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS
            ),
            Map.of(   // 6: red nether brick
                    Blocks.BRICKS, Blocks.RED_NETHER_BRICKS,
                    Blocks.BRICK_STAIRS, Blocks.RED_NETHER_BRICK_STAIRS
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
        int materialIndex = StairMaterialContext.get();
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
        return ModProcessors.STAIR_MATERIAL;
    }
}
