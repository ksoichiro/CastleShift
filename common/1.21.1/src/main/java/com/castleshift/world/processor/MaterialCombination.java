package com.castleshift.world.processor;

import java.util.List;

public record MaterialCombination(int roofIndex, int wallIndex) {
    static final List<MaterialCombination> ALLOWED_COMBINATIONS = List.of(
            // wall: stone bricks (0)
            new MaterialCombination(0, 0), // roof: spruce
            new MaterialCombination(1, 0), // roof: dark oak
            new MaterialCombination(2, 0), // roof: deepslate bricks
            new MaterialCombination(3, 0), // roof: dark prismarine
            new MaterialCombination(4, 0), // roof: red nether bricks
            new MaterialCombination(5, 0), // roof: crimson
            // wall: deepslate brick (1)
            new MaterialCombination(1, 1), // roof: dark oak
            new MaterialCombination(2, 1), // roof: deepslate bricks
            new MaterialCombination(3, 1), // roof: dark prismarine
            new MaterialCombination(4, 1), // roof: red nether bricks
            new MaterialCombination(5, 1), // roof: crimson
            // wall: polished blackstone brick (2)
            new MaterialCombination(1, 2), // roof: dark oak
            new MaterialCombination(3, 2), // roof: dark prismarine
            new MaterialCombination(4, 2), // roof: red nether bricks
            new MaterialCombination(5, 2), // roof: crimson
            // wall: sandstone (3)
            new MaterialCombination(0, 3), // roof: spruce
            new MaterialCombination(1, 3), // roof: dark oak
            new MaterialCombination(2, 3), // roof: deepslate bricks
            new MaterialCombination(3, 3), // roof: dark prismarine
            new MaterialCombination(4, 3), // roof: red nether bricks
            new MaterialCombination(5, 3), // roof: crimson
            // wall: end stone brick (4)
            new MaterialCombination(0, 4), // roof: spruce
            new MaterialCombination(1, 4), // roof: dark oak
            new MaterialCombination(2, 4), // roof: deepslate bricks
            new MaterialCombination(3, 4), // roof: dark prismarine
            new MaterialCombination(4, 4), // roof: red nether bricks
            new MaterialCombination(5, 4)  // roof: crimson
    );

    public static int combinationCount() {
        return ALLOWED_COMBINATIONS.size();
    }

    public static MaterialCombination get(int index) {
        return ALLOWED_COMBINATIONS.get(index);
    }
}
