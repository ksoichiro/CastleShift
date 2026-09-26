// Shared fabric/base override for the 26.x family: KeyMapping.Category/fabric-key-mapping-api-v1 differ from the 1.20.1/1.21.x family, so this stays a separate override even after they folded back into fabric/base.
// Fold this directory back into fabric/base once the other versions gain that package. Do not grow this into a per-version copy pattern.
package com.castleshift.fabric;

import com.castleshift.CastleShift;
import com.castleshift.config.ConfigLoader;
import com.castleshift.world.placement.ModPlacements;
import com.castleshift.world.processor.ModProcessors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class CastleShiftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModProcessors.init();
        ModPlacements.init();
        // Common init runs on both client and dedicated server, and finishes long before worldgen,
        // so a hand-edited config file is respected even if the config screen is never opened.
        ConfigLoader.loadAndApply(
                FabricLoader.getInstance().getConfigDir().resolve(ConfigLoader.CONFIG_FILE_NAME));
        CastleShift.init();
    }
}
