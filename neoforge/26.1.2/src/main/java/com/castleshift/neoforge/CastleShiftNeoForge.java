package com.castleshift.neoforge;

import com.castleshift.CastleShift;
import com.castleshift.world.processor.ModProcessors;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

// MC 26.1.2: BuiltInRegistries.STRUCTURE_PROCESSOR is Registry<StructureProcessorType<?>> (same as 1.21.x).
// Registration is done directly via ModProcessors.init() rather than DeferredRegister.
@Mod(CastleShift.MOD_ID)
public class CastleShiftNeoForge {
    public CastleShiftNeoForge(IEventBus modEventBus) {
        ModProcessors.init();
        CastleShift.init();
    }
}
