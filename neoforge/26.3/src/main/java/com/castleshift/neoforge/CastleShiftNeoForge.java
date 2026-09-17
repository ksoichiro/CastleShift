package com.castleshift.neoforge;

import com.castleshift.CastleShift;
import com.castleshift.world.processor.RoofMaterialProcessor;
import com.castleshift.world.processor.StairMaterialProcessor;
import com.castleshift.world.processor.WallMaterialProcessor;
import com.castleshift.world.processor.WallWeatheringProcessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

// MC 26.2: BuiltInRegistries.STRUCTURE_PROCESSOR is now Registry<MapCodec<? extends StructureProcessor>>.
// Use DeferredRegister on the mod event bus (not direct Registry.register in the constructor,
// which would fail with IllegalStateException on frozen registry).
@Mod(CastleShift.MOD_ID)
public class CastleShiftNeoForge {
    @SuppressWarnings("unused")
    public static final DeferredRegister<MapCodec<? extends StructureProcessor>> PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, CastleShift.MOD_ID);

    static {
        PROCESSOR_TYPES.register("roof_material", () -> RoofMaterialProcessor.CODEC);
        PROCESSOR_TYPES.register("wall_material", () -> WallMaterialProcessor.CODEC);
        PROCESSOR_TYPES.register("stair_material", () -> StairMaterialProcessor.CODEC);
        PROCESSOR_TYPES.register("wall_weathering", () -> WallWeatheringProcessor.CODEC);
    }

    public CastleShiftNeoForge(IEventBus modEventBus) {
        PROCESSOR_TYPES.register(modEventBus);
        CastleShift.init();
    }
}
