// Temporary 1.21.8-only override directory (see CastleShiftForge): the config screen classes need
// com.castleshift.world.placement. Fold back into forge/base-56 once every forgeMajor>=56 version has it.
package com.castleshift.forge;

import com.castleshift.config.ConfigLoader;
import com.castleshift.config.client.ClientConfigKeybind;
import com.castleshift.config.client.ConfigScreen;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Client-only registrations for the Forge 56+ (EventBus 7) entrypoint. Forge allows a single
 * {@code @Mod} class per mod id, so unlike NeoForge this cannot be a second entrypoint:
 * {@link CastleShiftForge} calls {@link #init} behind a {@code FMLEnvironment.dist.isClient()}
 * check so these client classes are never loaded on a dedicated server.
 */
public final class CastleShiftForgeClient {

    private CastleShiftForgeClient() {}

    @SuppressWarnings("removal")
    public static void init(FMLJavaModLoadingContext context) {
        // EventBus 7 events are typed per-class; RegisterKeyMappingsEvent exposes its own
        // per-BusGroup EventBus via getBus(), and ClientTickEvent.Post exposes a static BUS field.
        RegisterKeyMappingsEvent.getBus(context.getModBusGroup())
                .addListener(CastleShiftForgeClient::onRegisterKeyMappings);
        TickEvent.ClientTickEvent.Post.BUS.addListener(CastleShiftForgeClient::onClientTick);

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, modListScreen) -> new ConfigScreen(modListScreen, configFile())));
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientConfigKeybind.OPEN_CONFIG_SCREEN);
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        ClientConfigKeybind.onClientTick(Minecraft.getInstance(), configFile());
    }

    private static Path configFile() {
        return FMLPaths.CONFIGDIR.get().resolve(ConfigLoader.CONFIG_FILE_NAME);
    }
}
