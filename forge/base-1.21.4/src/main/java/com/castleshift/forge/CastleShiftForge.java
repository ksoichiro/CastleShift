// Temporary 1.21.4-only override of the shared forge/base entrypoint (forgeMajor < 56): com.castleshift.world.placement (ModPlacements) exists only under a subset of versions so far.
// Fold this directory back into forge/base once the other versions gain that package. Do not grow this into a per-version copy pattern.
package com.castleshift.forge;

import com.castleshift.CastleShift;
import com.castleshift.config.ConfigLoader;
import com.castleshift.world.placement.ModPlacements;
import com.castleshift.world.processor.ModProcessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;

@Mod(CastleShift.MOD_ID)
public class CastleShiftForge {
    @SuppressWarnings("unused")
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, CastleShift.MOD_ID);

    @SuppressWarnings("unused")
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENT_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, CastleShift.MOD_ID);

    static {
        PROCESSOR_TYPES.register("roof_material", () -> ModProcessors.ROOF_MATERIAL);
        PROCESSOR_TYPES.register("wall_material", () -> ModProcessors.WALL_MATERIAL);
        PROCESSOR_TYPES.register("stair_material", () -> ModProcessors.STAIR_MATERIAL);
        PROCESSOR_TYPES.register("wall_weathering", () -> ModProcessors.WALL_WEATHERING);
        PLACEMENT_TYPES.register("configurable_spread", () -> ModPlacements.CONFIGURABLE_SPREAD);
    }

    public CastleShiftForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        PROCESSOR_TYPES.register(modEventBus);
        PLACEMENT_TYPES.register(modEventBus);
        // Common init runs on both client and dedicated server, and finishes long before worldgen,
        // so a hand-edited config file is respected even if the config screen is never opened.
        ConfigLoader.loadAndApply(FMLPaths.CONFIGDIR.get().resolve(ConfigLoader.CONFIG_FILE_NAME));
        CastleShift.init();

        // Keep client-only classes (Minecraft/Screen/ConfigScreenHandler) off the dedicated server.
        if (FMLEnvironment.dist.isClient()) {
            CastleShiftForgeClient.init(modEventBus);
        }
    }
}
