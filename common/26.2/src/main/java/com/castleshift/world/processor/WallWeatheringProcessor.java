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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

// MC 26.x: StructureProcessor is now an interface; processBlock signature changed
public class WallWeatheringProcessor implements StructureProcessor {
    public static final WallWeatheringProcessor INSTANCE = new WallWeatheringProcessor();
    public static final MapCodec<WallWeatheringProcessor> CODEC = MapCodec.unit(INSTANCE);

    private static final float WEATHERING_CHANCE = 0.1f;
    private static final Map<Block, Block[]> WEATHERING_MAP = Map.of(
            Blocks.STONE_BRICKS, new Block[]{Blocks.COBBLESTONE, Blocks.ANDESITE},
            Blocks.POLISHED_BLACKSTONE_BRICKS, new Block[]{Blocks.BLACKSTONE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS}
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
        if (!WallWeatheringContext.isEnabled()) {
            return blockInfo;
        }
        Block block = blockInfo.state().getBlock();
        Block[] replacements = WEATHERING_MAP.get(block);
        if (replacements == null) {
            return blockInfo;
        }

        RandomSource random = settings.getRandom(blockInfo.pos());
        if (random.nextFloat() >= WEATHERING_CHANCE) {
            return blockInfo;
        }

        Block replacement = replacements[random.nextInt(replacements.length)];
        return new StructureTemplate.StructureBlockInfo(
                blockInfo.pos(),
                replacement.defaultBlockState(),
                blockInfo.nbt());
    }
}
