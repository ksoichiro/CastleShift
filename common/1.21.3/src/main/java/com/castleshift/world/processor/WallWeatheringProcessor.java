package com.castleshift.world.processor;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class WallWeatheringProcessor extends StructureProcessor {
    public static final WallWeatheringProcessor INSTANCE = new WallWeatheringProcessor();
    public static final MapCodec<WallWeatheringProcessor> CODEC = MapCodec.unit(INSTANCE);

    private static final float WEATHERING_CHANCE = 0.1f;
    private static final Map<Block, Block[]> WEATHERING_MAP = Map.of(
            Blocks.STONE_BRICKS, new Block[]{Blocks.COBBLESTONE, Blocks.ANDESITE},
            Blocks.POLISHED_BLACKSTONE_BRICKS, new Block[]{Blocks.BLACKSTONE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS}
    );

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos blockPos,
            BlockPos pos,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo modifiedBlockInfo,
            StructurePlaceSettings settings) {
        if (!WallWeatheringContext.isEnabled()) {
            return modifiedBlockInfo;
        }
        Block block = modifiedBlockInfo.state().getBlock();
        Block[] replacements = WEATHERING_MAP.get(block);
        if (replacements == null) {
            return modifiedBlockInfo;
        }

        RandomSource random = settings.getRandom(modifiedBlockInfo.pos());
        if (random.nextFloat() >= WEATHERING_CHANCE) {
            return modifiedBlockInfo;
        }

        Block replacement = replacements[random.nextInt(replacements.length)];
        return new StructureTemplate.StructureBlockInfo(
                modifiedBlockInfo.pos(),
                replacement.defaultBlockState(),
                modifiedBlockInfo.nbt());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModProcessors.WALL_WEATHERING;
    }
}
