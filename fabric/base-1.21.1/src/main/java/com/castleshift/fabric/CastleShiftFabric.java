// Temporary 1.21.1-only override of the shared fabric/base entrypoint: com.castleshift.world.placement (ModPlacements) exists only under common/1.21.1.
// Fold this directory back into fabric/base once the other versions gain that package. Do not grow this into a per-version copy pattern.
package com.castleshift.fabric;

import com.castleshift.CastleShift;
import com.castleshift.world.placement.ModPlacements;
import com.castleshift.world.processor.ModProcessors;
import net.fabricmc.api.ModInitializer;

public class CastleShiftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModProcessors.init();
        ModPlacements.init();
        CastleShift.init();
    }
}
