package com.castleshift.world.processor;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

// MC 26.1.2: BuiltInRegistries.STRUCTURE_PROCESSOR is Registry<StructureProcessorType<?>>
// (same as 1.21.x); register StructureProcessorType wrappers, not MapCodecs directly.
public class ModProcessors {
    public static final StructureProcessorType<RoofMaterialProcessor> ROOF_MATERIAL = () -> RoofMaterialProcessor.CODEC;
    public static final StructureProcessorType<WallMaterialProcessor> WALL_MATERIAL = () -> WallMaterialProcessor.CODEC;
    public static final StructureProcessorType<StairMaterialProcessor> STAIR_MATERIAL = () -> StairMaterialProcessor.CODEC;
    public static final StructureProcessorType<WallWeatheringProcessor> WALL_WEATHERING = () -> WallWeatheringProcessor.CODEC;

    public static void init() {
        Identifier roofId = Identifier.fromNamespaceAndPath("castleshift", "roof_material");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(roofId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, roofId, ROOF_MATERIAL);
        }
        Identifier wallId = Identifier.fromNamespaceAndPath("castleshift", "wall_material");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(wallId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, wallId, WALL_MATERIAL);
        }
        Identifier stairId = Identifier.fromNamespaceAndPath("castleshift", "stair_material");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(stairId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, stairId, STAIR_MATERIAL);
        }
        Identifier wallWeatheringId = Identifier.fromNamespaceAndPath("castleshift", "wall_weathering");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(wallWeatheringId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, wallWeatheringId, WALL_WEATHERING);
        }
    }
}
