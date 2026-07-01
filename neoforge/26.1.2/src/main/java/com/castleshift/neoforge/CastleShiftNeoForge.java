package com.castleshift.neoforge;

import com.castleshift.CastleShift;
import com.castleshift.world.processor.ModProcessors;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

// MC 26.x: BuiltInRegistries.STRUCTURE_PROCESSOR changed from Registry<StructureProcessorType<?>>
// to Registry<MapCodec<? extends StructureProcessor>>. DeferredRegister<StructureProcessorType<?>>
// no longer applies; registration is done directly via ModProcessors.init().
@Mod(CastleShift.MOD_ID)
public class CastleShiftNeoForge {
    public CastleShiftNeoForge(IEventBus modEventBus) {
        ModProcessors.init();
        CastleShift.init();
    }
}
