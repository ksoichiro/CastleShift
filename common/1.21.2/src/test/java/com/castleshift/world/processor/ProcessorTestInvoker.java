package com.castleshift.world.processor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

// Old-signature version (1.21.x, 26.1.2): processBlock(level, blockPos, pos, originalInfo, modifiedInfo, settings)
final class ProcessorTestInvoker {
    private ProcessorTestInvoker() {}

    static StructureTemplate.StructureBlockInfo process(
            StructureProcessor processor, StructureTemplate.StructureBlockInfo info) {
        return processor.processBlock(
                null, BlockPos.ZERO, BlockPos.ZERO, info, info, new StructurePlaceSettings());
    }
}
