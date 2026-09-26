package com.castleshift.world.placement;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class ModPlacements {
    public static final StructurePlacementType<ConfigurableSpreadStructurePlacement> CONFIGURABLE_SPREAD =
            () -> ConfigurableSpreadStructurePlacement.CODEC;

    public static void init() {
        Identifier id = Identifier.fromNamespaceAndPath("castleshift", "configurable_spread");
        if (!BuiltInRegistries.STRUCTURE_PLACEMENT.containsKey(id)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, id, CONFIGURABLE_SPREAD);
        }
    }
}
