package com.castleshift.world.processor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

// MC 26.x: StructureProcessorType is no longer generic;
// BuiltInRegistries.STRUCTURE_PROCESSOR is now Registry<MapCodec<? extends StructureProcessor>>.
// Register MapCodecs directly instead of StructureProcessorType wrappers.
public class ModProcessors {

    public static void init() {
        Identifier roofId = Identifier.fromNamespaceAndPath("castleshift", "roof_material");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(roofId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, roofId, RoofMaterialProcessor.CODEC);
        }
        Identifier wallId = Identifier.fromNamespaceAndPath("castleshift", "wall_material");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(wallId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, wallId, WallMaterialProcessor.CODEC);
        }
        Identifier stairId = Identifier.fromNamespaceAndPath("castleshift", "stair_material");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(stairId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, stairId, StairMaterialProcessor.CODEC);
        }
        Identifier wallWeatheringId = Identifier.fromNamespaceAndPath("castleshift", "wall_weathering");
        if (!BuiltInRegistries.STRUCTURE_PROCESSOR.containsKey(wallWeatheringId)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, wallWeatheringId, WallWeatheringProcessor.CODEC);
        }
    }
}
