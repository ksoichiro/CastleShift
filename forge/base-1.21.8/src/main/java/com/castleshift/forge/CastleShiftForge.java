// Temporary 1.21.8-only override of the shared forge/base-56 entrypoint (forgeMajor >= 56, EventBus 7):
// com.castleshift.world.placement (ModPlacements) exists only under a subset of versions so far.
// Fold this directory back into forge/base-56 once every forgeMajor>=56 version has that package.
package com.castleshift.forge;

import com.castleshift.CastleShift;
import com.castleshift.config.ConfigLoader;
import com.castleshift.world.placement.ModPlacements;
import com.castleshift.world.processor.ModProcessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
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

    @SuppressWarnings("removal")
    public CastleShiftForge(FMLJavaModLoadingContext context) {
        PROCESSOR_TYPES.register(context.getModBusGroup());
        PLACEMENT_TYPES.register(context.getModBusGroup());
        // Common init runs on both client and dedicated server, and finishes long before worldgen,
        // so a hand-edited config file is respected even if the config screen is never opened.
        ConfigLoader.loadAndApply(FMLPaths.CONFIGDIR.get().resolve(ConfigLoader.CONFIG_FILE_NAME));
        CastleShift.init();

        // Keep client-only classes (Minecraft/Screen/ConfigScreenHandler) off the dedicated server.
        if (FMLEnvironment.dist.isClient()) {
            CastleShiftForgeClient.init(context);
        }
    }
}
