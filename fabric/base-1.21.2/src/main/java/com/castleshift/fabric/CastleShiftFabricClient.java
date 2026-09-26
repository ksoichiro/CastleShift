// Temporary 1.21.2-only override of the shared fabric/base entrypoints (see CastleShiftFabric); fold back into base/ once other versions gain com.castleshift.world.placement.
package com.castleshift.fabric;

import com.castleshift.config.ConfigLoader;
import com.castleshift.config.client.ClientConfigKeybind;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;

public class CastleShiftFabricClient implements ClientModInitializer {

    /** Shared with the (optional) ModMenu integration so both open the same file. */
    public static Path configFile() {
        return FabricLoader.getInstance().getConfigDir().resolve(ConfigLoader.CONFIG_FILE_NAME);
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ClientConfigKeybind.OPEN_CONFIG_SCREEN);
        ClientTickEvents.END_CLIENT_TICK.register(
                minecraft -> ClientConfigKeybind.onClientTick(minecraft, configFile()));
    }
}
