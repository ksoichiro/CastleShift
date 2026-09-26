package com.castleshift.world.placement;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

// MC 26.3: StructurePlacementType is gone; BuiltInRegistries.STRUCTURE_PLACEMENT is now
// Registry<MapCodec<? extends StructurePlacement>>. Register the MapCodec directly, same as
// ModProcessors does for StructureProcessor since the MC 26.2 split.
public class ModPlacements {
    public static void init() {
        Identifier id = Identifier.fromNamespaceAndPath("castleshift", "configurable_spread");
        if (!BuiltInRegistries.STRUCTURE_PLACEMENT.containsKey(id)) {
            Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, id, ConfigurableSpreadStructurePlacement.CODEC);
        }
    }
}
