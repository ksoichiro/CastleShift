// Optional ModMenu integration. ModMenu is a modCompileOnly dependency, so this class is only
// ever loaded when ModMenu is actually installed (it is reached through ModMenu's own entrypoint).
package com.castleshift.fabric.modmenu;

import com.castleshift.config.client.ClientConfigKeybind;
import com.castleshift.config.client.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class CastleShiftModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(
                parent,
                FabricLoader.getInstance().getConfigDir().resolve(ClientConfigKeybind.CONFIG_FILE_NAME));
    }
}
