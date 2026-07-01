package com.castleshift.forge;

import com.castleshift.CastleShift;
import com.castleshift.world.processor.ModProcessors;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// MC 26.1.2: BuiltInRegistries.STRUCTURE_PROCESSOR is Registry<StructureProcessorType<?>> (same as 1.21.x).
// Registration is done directly via ModProcessors.init() rather than DeferredRegister.
@Mod(CastleShift.MOD_ID)
public class CastleShiftForge {
    @SuppressWarnings("removal")
    public CastleShiftForge(FMLJavaModLoadingContext context) {
        ModProcessors.init();
        CastleShift.init();
    }
}
