package com.castleshift.world.processor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

// New-signature version (26.2): processBlock(level, blockPos, pos, offset, blockInfo, settings)
// StructureProcessor is now an interface; the old two-info parameter was replaced by a single info + offset.
final class ProcessorTestInvoker {
    private ProcessorTestInvoker() {}

    static StructureTemplate.StructureBlockInfo process(
            StructureProcessor processor, StructureTemplate.StructureBlockInfo info) {
        return processor.processBlock(
                null, BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, info, new StructurePlaceSettings());
    }
}
