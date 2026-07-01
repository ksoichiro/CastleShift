package com.castleshift.forge;

import com.castleshift.CastleShift;
import com.castleshift.world.processor.ModProcessors;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// MC 26.x: BuiltInRegistries.STRUCTURE_PROCESSOR changed from Registry<StructureProcessorType<?>>
// to Registry<MapCodec<? extends StructureProcessor>>. DeferredRegister<StructureProcessorType<?>>
// no longer applies; registration is done directly via ModProcessors.init().
@Mod(CastleShift.MOD_ID)
public class CastleShiftForge {
    @SuppressWarnings("removal")
    public CastleShiftForge(FMLJavaModLoadingContext context) {
        ModProcessors.init();
        CastleShift.init();
    }
}
