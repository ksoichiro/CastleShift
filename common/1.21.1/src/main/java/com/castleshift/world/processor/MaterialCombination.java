package com.castleshift.world.processor;

import static com.castleshift.world.processor.RoofMaterialProcessor.CRIMSON;
import static com.castleshift.world.processor.RoofMaterialProcessor.DARK_OAK;
import static com.castleshift.world.processor.RoofMaterialProcessor.DARK_PRISMARINE;
import static com.castleshift.world.processor.RoofMaterialProcessor.SPRUCE;
import static com.castleshift.world.processor.StairMaterialProcessor.BRICKS;
import static com.castleshift.world.processor.StairMaterialProcessor.POLISHED_BLACKSTONE;
import static com.castleshift.world.processor.StairMaterialProcessor.POLISHED_GRANITE;
import static com.castleshift.world.processor.WallMaterialProcessor.END_STONE_BRICKS;
import static com.castleshift.world.processor.WallMaterialProcessor.SANDSTONE;

import java.util.List;

public record MaterialCombination(int roofIndex, int wallIndex, int stairIndex) {
    static final List<MaterialCombination> ALLOWED_COMBINATIONS = List.of(
            // wall: stone bricks, stair: bricks/stone bricks/polished granite
            new MaterialCombination(SPRUCE, WallMaterialProcessor.STONE_BRICKS, BRICKS),
            new MaterialCombination(SPRUCE, WallMaterialProcessor.STONE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(SPRUCE, WallMaterialProcessor.STONE_BRICKS, POLISHED_GRANITE),
            new MaterialCombination(DARK_OAK, WallMaterialProcessor.STONE_BRICKS, BRICKS),
            new MaterialCombination(DARK_OAK, WallMaterialProcessor.STONE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(DARK_OAK, WallMaterialProcessor.STONE_BRICKS, POLISHED_GRANITE),
            new MaterialCombination(RoofMaterialProcessor.DEEPSLATE_BRICKS, WallMaterialProcessor.STONE_BRICKS, BRICKS),
            new MaterialCombination(RoofMaterialProcessor.DEEPSLATE_BRICKS, WallMaterialProcessor.STONE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(RoofMaterialProcessor.DEEPSLATE_BRICKS, WallMaterialProcessor.STONE_BRICKS, POLISHED_GRANITE),
            new MaterialCombination(DARK_PRISMARINE, WallMaterialProcessor.STONE_BRICKS, BRICKS),
            new MaterialCombination(DARK_PRISMARINE, WallMaterialProcessor.STONE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(DARK_PRISMARINE, WallMaterialProcessor.STONE_BRICKS, POLISHED_GRANITE),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, WallMaterialProcessor.STONE_BRICKS, BRICKS),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, WallMaterialProcessor.STONE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, WallMaterialProcessor.STONE_BRICKS, POLISHED_GRANITE),
            new MaterialCombination(CRIMSON, WallMaterialProcessor.STONE_BRICKS, BRICKS),
            new MaterialCombination(CRIMSON, WallMaterialProcessor.STONE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(CRIMSON, WallMaterialProcessor.STONE_BRICKS, POLISHED_GRANITE),
            // wall: deepslate bricks, stair: bricks/stone bricks/deepslate bricks
            new MaterialCombination(DARK_OAK, WallMaterialProcessor.DEEPSLATE_BRICKS, BRICKS),
            new MaterialCombination(DARK_OAK, WallMaterialProcessor.DEEPSLATE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(DARK_OAK, WallMaterialProcessor.DEEPSLATE_BRICKS, StairMaterialProcessor.DEEPSLATE_BRICKS),
            new MaterialCombination(RoofMaterialProcessor.DEEPSLATE_BRICKS, WallMaterialProcessor.DEEPSLATE_BRICKS, BRICKS),
            new MaterialCombination(RoofMaterialProcessor.DEEPSLATE_BRICKS, WallMaterialProcessor.DEEPSLATE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(RoofMaterialProcessor.DEEPSLATE_BRICKS, WallMaterialProcessor.DEEPSLATE_BRICKS, StairMaterialProcessor.DEEPSLATE_BRICKS),
            new MaterialCombination(DARK_PRISMARINE, WallMaterialProcessor.DEEPSLATE_BRICKS, BRICKS),
            new MaterialCombination(DARK_PRISMARINE, WallMaterialProcessor.DEEPSLATE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(DARK_PRISMARINE, WallMaterialProcessor.DEEPSLATE_BRICKS, StairMaterialProcessor.DEEPSLATE_BRICKS),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, WallMaterialProcessor.DEEPSLATE_BRICKS, BRICKS),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, WallMaterialProcessor.DEEPSLATE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, WallMaterialProcessor.DEEPSLATE_BRICKS, StairMaterialProcessor.DEEPSLATE_BRICKS),
            new MaterialCombination(CRIMSON, WallMaterialProcessor.DEEPSLATE_BRICKS, BRICKS),
            new MaterialCombination(CRIMSON, WallMaterialProcessor.DEEPSLATE_BRICKS, StairMaterialProcessor.STONE_BRICKS),
            new MaterialCombination(CRIMSON, WallMaterialProcessor.DEEPSLATE_BRICKS, StairMaterialProcessor.DEEPSLATE_BRICKS),
            // wall: polished blackstone bricks, stair: polished blackstone/polished blackstone bricks/red nether bricks
            new MaterialCombination(DARK_OAK, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, POLISHED_BLACKSTONE),
            new MaterialCombination(DARK_OAK, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, StairMaterialProcessor.POLISHED_BLACKSTONE_BRICKS),
            new MaterialCombination(DARK_OAK, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, StairMaterialProcessor.RED_NETHER_BRICKS),
            new MaterialCombination(DARK_PRISMARINE, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, POLISHED_BLACKSTONE),
            new MaterialCombination(DARK_PRISMARINE, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, StairMaterialProcessor.POLISHED_BLACKSTONE_BRICKS),
            new MaterialCombination(DARK_PRISMARINE, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, StairMaterialProcessor.RED_NETHER_BRICKS),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, POLISHED_BLACKSTONE),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, StairMaterialProcessor.POLISHED_BLACKSTONE_BRICKS),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, StairMaterialProcessor.RED_NETHER_BRICKS),
            new MaterialCombination(CRIMSON, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, POLISHED_BLACKSTONE),
            new MaterialCombination(CRIMSON, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, StairMaterialProcessor.POLISHED_BLACKSTONE_BRICKS),
            new MaterialCombination(CRIMSON, WallMaterialProcessor.POLISHED_BLACKSTONE_BRICKS, StairMaterialProcessor.RED_NETHER_BRICKS),
            // wall: sandstone, stair: bricks/polished granite
            new MaterialCombination(SPRUCE, SANDSTONE, BRICKS),
            new MaterialCombination(SPRUCE, SANDSTONE, POLISHED_GRANITE),
            new MaterialCombination(DARK_OAK, SANDSTONE, BRICKS),
            new MaterialCombination(DARK_OAK, SANDSTONE, POLISHED_GRANITE),
            new MaterialCombination(RoofMaterialProcessor.DEEPSLATE_BRICKS, SANDSTONE, BRICKS),
            new MaterialCombination(RoofMaterialProcessor.DEEPSLATE_BRICKS, SANDSTONE, POLISHED_GRANITE),
            new MaterialCombination(DARK_PRISMARINE, SANDSTONE, BRICKS),
            new MaterialCombination(DARK_PRISMARINE, SANDSTONE, POLISHED_GRANITE),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, SANDSTONE, BRICKS),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, SANDSTONE, POLISHED_GRANITE),
            new MaterialCombination(CRIMSON, SANDSTONE, BRICKS),
            new MaterialCombination(CRIMSON, SANDSTONE, POLISHED_GRANITE),
            // wall: end stone bricks, stair: bricks/polished granite
            new MaterialCombination(SPRUCE, END_STONE_BRICKS, BRICKS),
            new MaterialCombination(SPRUCE, END_STONE_BRICKS, POLISHED_GRANITE),
            new MaterialCombination(DARK_OAK, END_STONE_BRICKS, BRICKS),
            new MaterialCombination(DARK_OAK, END_STONE_BRICKS, POLISHED_GRANITE),
            new MaterialCombination(RoofMaterialProcessor.DEEPSLATE_BRICKS, END_STONE_BRICKS, BRICKS),
            new MaterialCombination(RoofMaterialProcessor.DEEPSLATE_BRICKS, END_STONE_BRICKS, POLISHED_GRANITE),
            new MaterialCombination(DARK_PRISMARINE, END_STONE_BRICKS, BRICKS),
            new MaterialCombination(DARK_PRISMARINE, END_STONE_BRICKS, POLISHED_GRANITE),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, END_STONE_BRICKS, BRICKS),
            new MaterialCombination(RoofMaterialProcessor.RED_NETHER_BRICKS, END_STONE_BRICKS, POLISHED_GRANITE),
            new MaterialCombination(CRIMSON, END_STONE_BRICKS, BRICKS),
            new MaterialCombination(CRIMSON, END_STONE_BRICKS, POLISHED_GRANITE)
    );

    public static int combinationCount() {
        return ALLOWED_COMBINATIONS.size();
    }

    public static MaterialCombination get(int index) {
        return ALLOWED_COMBINATIONS.get(index);
    }
}
