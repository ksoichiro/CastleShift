package com.castleshift.world.processor;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class ModProcessors {
    public static StructureProcessorType<RoofMaterialProcessor> ROOF_MATERIAL = () -> RoofMaterialProcessor.CODEC;
    public static StructureProcessorType<WallMaterialProcessor> WALL_MATERIAL = () -> WallMaterialProcessor.CODEC;
    public static StructureProcessorType<StairMaterialProcessor> STAIR_MATERIAL = () -> StairMaterialProcessor.CODEC;
    public static StructureProcessorType<WallWeatheringProcessor> WALL_WEATHERING = () -> WallWeatheringProcessor.CODEC;

    public static void init() {
        ResourceLocation roofId = new ResourceLocation("castleshift", "roof_material");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(roofId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, roofId, ROOF_MATERIAL);
        }
        ResourceLocation wallId = new ResourceLocation("castleshift", "wall_material");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(wallId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, wallId, WALL_MATERIAL);
        }
        ResourceLocation stairId = new ResourceLocation("castleshift", "stair_material");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(stairId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, stairId, STAIR_MATERIAL);
        }
        ResourceLocation wallWeatheringId = new ResourceLocation("castleshift", "wall_weathering");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(wallWeatheringId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, wallWeatheringId, WALL_WEATHERING);
        }
    }
}
