package com.castleshift.forge;

import com.castleshift.CastleShift;
import com.castleshift.world.processor.ModProcessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

@Mod(CastleShift.MOD_ID)
public class CastleShiftForge {
    @SuppressWarnings("unused")
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, CastleShift.MOD_ID);

    static {
        PROCESSOR_TYPES.register("roof_material", () -> ModProcessors.ROOF_MATERIAL);
    }

    public CastleShiftForge() {
        PROCESSOR_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CastleShift.init();
    }
}
