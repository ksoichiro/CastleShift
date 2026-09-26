// Temporary 1.21.11-only override directory (see CastleShiftNeoForge): the config screen classes need com.castleshift.world.placement. Fold back into neoforge/base once every version has it.
// live under common/1.21.1 only. Fold back into neoforge/base once the other versions gain them.
package com.castleshift.neoforge;

import com.castleshift.CastleShift;
import com.castleshift.config.ConfigLoader;
import com.castleshift.config.client.ClientConfigKeybind;
import com.castleshift.config.client.ConfigScreen;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only {@code @Mod} entrypoint. NeoForge allows several {@code @Mod} classes per mod id and
 * filters them by {@code dist}, so everything touching {@code Minecraft}/{@code Screen} stays out of
 * {@link CastleShiftNeoForge} and is never class-loaded on a dedicated server.
 */
@Mod(value = CastleShift.MOD_ID, dist = Dist.CLIENT)
public class CastleShiftNeoForgeClient {

    public CastleShiftNeoForgeClient(ModContainer container, IEventBus modEventBus) {
        // RegisterKeyMappingsEvent implements IModBusEvent -> mod bus.
        modEventBus.addListener(CastleShiftNeoForgeClient::onRegisterKeyMappings);
        // ClientTickEvent is a plain Event (not IModBusEvent) -> game bus.
        NeoForge.EVENT_BUS.addListener(CastleShiftNeoForgeClient::onClientTick);

        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (modContainer, modListScreen) -> new ConfigScreen(modListScreen, configFile()));
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientConfigKeybind.OPEN_CONFIG_SCREEN);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        ClientConfigKeybind.onClientTick(Minecraft.getInstance(), configFile());
    }

    private static Path configFile() {
        return FMLPaths.CONFIGDIR.get().resolve(ConfigLoader.CONFIG_FILE_NAME);
    }
}
