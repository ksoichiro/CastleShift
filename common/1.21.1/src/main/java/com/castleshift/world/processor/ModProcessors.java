package com.castleshift.world.processor;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class ModProcessors {
    public static final StructureProcessorType<RoofMaterialProcessor> ROOF_MATERIAL = () -> RoofMaterialProcessor.CODEC;

    public static void init() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("castleshift", "roof_material");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(id)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, id, ROOF_MATERIAL);
        }
    }
}
