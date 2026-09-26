package com.castleshift.world.placement;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class ModPlacements {
    public static final StructurePlacementType<ConfigurableSpreadStructurePlacement> CONFIGURABLE_SPREAD =
            () -> ConfigurableSpreadStructurePlacement.CODEC;

    public static void init() {
        // MC 1.20.1 has no ResourceLocation.fromNamespaceAndPath; use the two-arg constructor.
        ResourceLocation id = new ResourceLocation("castleshift", "configurable_spread");
        if (!BuiltInRegistries.STRUCTURE_PLACEMENT.containsKey(id)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, id, CONFIGURABLE_SPREAD);
        }
    }
}
