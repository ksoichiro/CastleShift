package com.castleshift.config.client;

import com.castleshift.config.ConfigLoader;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Client-only holder for the "open config screen" key mapping.
 *
 * <p>Must only ever be referenced from client-side code paths (Fabric client entrypoint,
 * NeoForge/Forge client-side registration): loading it on a dedicated server would pull in
 * {@link KeyMapping}/{@link Minecraft}.
 */
public final class ClientConfigKeybind {

    /**
     * Name of the config file, relative to each loader's config directory.
     *
     * <p>Alias of {@link ConfigLoader#CONFIG_FILE_NAME}, which is where the value now lives: this
     * class is client-only, so common/server init paths cannot reference it without dragging
     * {@link KeyMapping} onto a dedicated server.
     */
    public static final String CONFIG_FILE_NAME = ConfigLoader.CONFIG_FILE_NAME;

    /** Unbound by default so it cannot clash with vanilla or other mods' defaults. */
    public static final KeyMapping OPEN_CONFIG_SCREEN = new KeyMapping(
            "key.castleshift.open_config",
            InputConstants.UNKNOWN.getValue(),
            "key.categories.castleshift");

    private ClientConfigKeybind() {}

    public static void onClientTick(Minecraft minecraft, Path configFile) {
        if (minecraft.screen == null && OPEN_CONFIG_SCREEN.consumeClick()) {
            minecraft.setScreen(new ConfigScreen(null, configFile));
        }
    }
}
