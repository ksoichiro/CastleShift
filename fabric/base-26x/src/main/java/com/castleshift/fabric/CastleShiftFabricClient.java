// Shared fabric/base override for the 26.x family (see CastleShiftFabric for why this can't fold into fabric/base).
package com.castleshift.fabric;

import com.castleshift.config.ConfigLoader;
import com.castleshift.config.client.ClientConfigKeybind;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;

public class CastleShiftFabricClient implements ClientModInitializer {

    /** Shared with the (optional) ModMenu integration so both open the same file. */
    public static Path configFile() {
        return FabricLoader.getInstance().getConfigDir().resolve(ConfigLoader.CONFIG_FILE_NAME);
    }

    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(ClientConfigKeybind.OPEN_CONFIG_SCREEN);
        ClientTickEvents.END_CLIENT_TICK.register(
                minecraft -> ClientConfigKeybind.onClientTick(minecraft, configFile()));
    }
}
