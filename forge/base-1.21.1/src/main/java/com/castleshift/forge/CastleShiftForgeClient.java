// Temporary 1.21.1-only override directory (see CastleShiftForge): the config screen classes
// live under common/1.21.1 only. Fold back into forge/base once the other versions gain them.
package com.castleshift.forge;

import com.castleshift.config.client.ClientConfigKeybind;
import com.castleshift.config.client.ConfigScreen;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Client-only registrations. Forge allows a single {@code @Mod} class per mod id, so unlike
 * NeoForge this cannot be a second entrypoint: {@link CastleShiftForge} calls {@link #init} behind
 * a {@code FMLEnvironment.dist.isClient()} check so these client classes are never loaded on a
 * dedicated server.
 */
public final class CastleShiftForgeClient {

    private CastleShiftForgeClient() {}

    public static void init(IEventBus modEventBus) {
        // RegisterKeyMappingsEvent implements IModBusEvent -> mod bus.
        modEventBus.addListener(CastleShiftForgeClient::onRegisterKeyMappings);
        // TickEvent is a game-bus event; register(Class) picks up the @SubscribeEvent method below.
        MinecraftForge.EVENT_BUS.register(CastleShiftForgeClient.class);

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, modListScreen) -> new ConfigScreen(modListScreen, configFile())));
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientConfigKeybind.OPEN_CONFIG_SCREEN);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // Forge fires this twice per tick; only act once.
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientConfigKeybind.onClientTick(Minecraft.getInstance(), configFile());
    }

    private static Path configFile() {
        return FMLPaths.CONFIGDIR.get().resolve(ClientConfigKeybind.CONFIG_FILE_NAME);
    }
}
