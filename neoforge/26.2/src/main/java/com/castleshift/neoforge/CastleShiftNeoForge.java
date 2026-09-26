package com.castleshift.neoforge;

import com.castleshift.CastleShift;
import com.castleshift.config.ConfigLoader;
import com.castleshift.world.placement.ModPlacements;
import com.castleshift.world.processor.RoofMaterialProcessor;
import com.castleshift.world.processor.StairMaterialProcessor;
import com.castleshift.world.processor.WallMaterialProcessor;
import com.castleshift.world.processor.WallWeatheringProcessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.DeferredRegister;

// MC 26.2: BuiltInRegistries.STRUCTURE_PROCESSOR is now Registry<MapCodec<? extends StructureProcessor>>.
// Use DeferredRegister on the mod event bus (not direct Registry.register in the constructor,
// which would fail with IllegalStateException on frozen registry). StructurePlacementType is
// unaffected by this split: it is still Registry<StructurePlacementType<?>>, same as 1.21.x.
@Mod(CastleShift.MOD_ID)
public class CastleShiftNeoForge {
    @SuppressWarnings("unused")
    public static final DeferredRegister<MapCodec<? extends StructureProcessor>> PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, CastleShift.MOD_ID);

    @SuppressWarnings("unused")
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENT_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, CastleShift.MOD_ID);

    static {
        PROCESSOR_TYPES.register("roof_material", () -> RoofMaterialProcessor.CODEC);
        PROCESSOR_TYPES.register("wall_material", () -> WallMaterialProcessor.CODEC);
        PROCESSOR_TYPES.register("stair_material", () -> StairMaterialProcessor.CODEC);
        PROCESSOR_TYPES.register("wall_weathering", () -> WallWeatheringProcessor.CODEC);
        PLACEMENT_TYPES.register("configurable_spread", () -> ModPlacements.CONFIGURABLE_SPREAD);
    }

    public CastleShiftNeoForge(IEventBus modEventBus) {
        PROCESSOR_TYPES.register(modEventBus);
        PLACEMENT_TYPES.register(modEventBus);
        // Common init runs on both client and dedicated server, and finishes long before worldgen,
        // so a hand-edited config file is respected even if the config screen is never opened.
        ConfigLoader.loadAndApply(FMLPaths.CONFIGDIR.get().resolve(ConfigLoader.CONFIG_FILE_NAME));
        CastleShift.init();
    }
}
