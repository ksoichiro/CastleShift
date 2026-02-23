package com.castleshift.fabric;

import com.castleshift.CastleShift;
import net.fabricmc.api.ModInitializer;

public class CastleShiftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CastleShift.init();
    }
}
