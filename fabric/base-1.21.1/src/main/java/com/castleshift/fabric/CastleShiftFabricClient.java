// Temporary 1.21.1-only override of the shared fabric/base entrypoints (see CastleShiftFabric); fold back into base/ once other versions gain com.castleshift.world.placement.
package com.castleshift.fabric;

import net.fabricmc.api.ClientModInitializer;

public class CastleShiftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
    }
}
