package com.castleshift.neoforge;

import com.castleshift.CastleShift;
import com.castleshift.world.processor.ModProcessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CastleShift.MOD_ID)
public class CastleShiftNeoForge {
    @SuppressWarnings("unused")
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, CastleShift.MOD_ID);

    static {
        PROCESSOR_TYPES.register("roof_material", () -> ModProcessors.ROOF_MATERIAL);
        PROCESSOR_TYPES.register("wall_material", () -> ModProcessors.WALL_MATERIAL);
        PROCESSOR_TYPES.register("stair_material", () -> ModProcessors.STAIR_MATERIAL);
    }

    public CastleShiftNeoForge(IEventBus modEventBus) {
        PROCESSOR_TYPES.register(modEventBus);
        CastleShift.init();
    }
}
