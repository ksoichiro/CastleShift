package com.castleshift.world.processor;

import java.util.List;

public record MaterialCombination(int roofIndex, int wallIndex, int stairIndex) {
    // stair indices:
    // 0: bricks (original)
    // 1: stone bricks
    // 2: polished granite
    // 3: deepslate brick
    // 4: polished blackstone
    // 5: polished blackstone brick
    // 6: red nether brick
    static final List<MaterialCombination> ALLOWED_COMBINATIONS = List.of(
            // wall: stone bricks (0), stair: bricks/stone bricks/polished granite
            new MaterialCombination(0, 0, 0), // roof: spruce, stair: bricks
            new MaterialCombination(0, 0, 1), // roof: spruce, stair: stone bricks
            new MaterialCombination(0, 0, 2), // roof: spruce, stair: polished granite
            new MaterialCombination(1, 0, 0), // roof: dark oak, stair: bricks
            new MaterialCombination(1, 0, 1), // roof: dark oak, stair: stone bricks
            new MaterialCombination(1, 0, 2), // roof: dark oak, stair: polished granite
            new MaterialCombination(2, 0, 0), // roof: deepslate bricks, stair: bricks
            new MaterialCombination(2, 0, 1), // roof: deepslate bricks, stair: stone bricks
            new MaterialCombination(2, 0, 2), // roof: deepslate bricks, stair: polished granite
            new MaterialCombination(3, 0, 0), // roof: dark prismarine, stair: bricks
            new MaterialCombination(3, 0, 1), // roof: dark prismarine, stair: stone bricks
            new MaterialCombination(3, 0, 2), // roof: dark prismarine, stair: polished granite
            new MaterialCombination(4, 0, 0), // roof: red nether bricks, stair: bricks
            new MaterialCombination(4, 0, 1), // roof: red nether bricks, stair: stone bricks
            new MaterialCombination(4, 0, 2), // roof: red nether bricks, stair: polished granite
            new MaterialCombination(5, 0, 0), // roof: crimson, stair: bricks
            new MaterialCombination(5, 0, 1), // roof: crimson, stair: stone bricks
            new MaterialCombination(5, 0, 2), // roof: crimson, stair: polished granite
            // wall: deepslate brick (1), stair: bricks/stone bricks/deepslate brick
            new MaterialCombination(1, 1, 0), // roof: dark oak, stair: bricks
            new MaterialCombination(1, 1, 1), // roof: dark oak, stair: stone bricks
            new MaterialCombination(1, 1, 3), // roof: dark oak, stair: deepslate brick
            new MaterialCombination(2, 1, 0), // roof: deepslate bricks, stair: bricks
            new MaterialCombination(2, 1, 1), // roof: deepslate bricks, stair: stone bricks
            new MaterialCombination(2, 1, 3), // roof: deepslate bricks, stair: deepslate brick
            new MaterialCombination(3, 1, 0), // roof: dark prismarine, stair: bricks
            new MaterialCombination(3, 1, 1), // roof: dark prismarine, stair: stone bricks
            new MaterialCombination(3, 1, 3), // roof: dark prismarine, stair: deepslate brick
            new MaterialCombination(4, 1, 0), // roof: red nether bricks, stair: bricks
            new MaterialCombination(4, 1, 1), // roof: red nether bricks, stair: stone bricks
            new MaterialCombination(4, 1, 3), // roof: red nether bricks, stair: deepslate brick
            new MaterialCombination(5, 1, 0), // roof: crimson, stair: bricks
            new MaterialCombination(5, 1, 1), // roof: crimson, stair: stone bricks
            new MaterialCombination(5, 1, 3), // roof: crimson, stair: deepslate brick
            // wall: polished blackstone brick (2), stair: polished blackstone/polished blackstone brick/red nether brick
            new MaterialCombination(1, 2, 4), // roof: dark oak, stair: polished blackstone
            new MaterialCombination(1, 2, 5), // roof: dark oak, stair: polished blackstone brick
            new MaterialCombination(1, 2, 6), // roof: dark oak, stair: red nether brick
            new MaterialCombination(3, 2, 4), // roof: dark prismarine, stair: polished blackstone
            new MaterialCombination(3, 2, 5), // roof: dark prismarine, stair: polished blackstone brick
            new MaterialCombination(3, 2, 6), // roof: dark prismarine, stair: red nether brick
            new MaterialCombination(4, 2, 4), // roof: red nether bricks, stair: polished blackstone
            new MaterialCombination(4, 2, 5), // roof: red nether bricks, stair: polished blackstone brick
            new MaterialCombination(4, 2, 6), // roof: red nether bricks, stair: red nether brick
            new MaterialCombination(5, 2, 4), // roof: crimson, stair: polished blackstone
            new MaterialCombination(5, 2, 5), // roof: crimson, stair: polished blackstone brick
            new MaterialCombination(5, 2, 6), // roof: crimson, stair: red nether brick
            // wall: sandstone (3), stair: bricks/polished granite
            new MaterialCombination(0, 3, 0), // roof: spruce, stair: bricks
            new MaterialCombination(0, 3, 2), // roof: spruce, stair: polished granite
            new MaterialCombination(1, 3, 0), // roof: dark oak, stair: bricks
            new MaterialCombination(1, 3, 2), // roof: dark oak, stair: polished granite
            new MaterialCombination(2, 3, 0), // roof: deepslate bricks, stair: bricks
            new MaterialCombination(2, 3, 2), // roof: deepslate bricks, stair: polished granite
            new MaterialCombination(3, 3, 0), // roof: dark prismarine, stair: bricks
            new MaterialCombination(3, 3, 2), // roof: dark prismarine, stair: polished granite
            new MaterialCombination(4, 3, 0), // roof: red nether bricks, stair: bricks
            new MaterialCombination(4, 3, 2), // roof: red nether bricks, stair: polished granite
            new MaterialCombination(5, 3, 0), // roof: crimson, stair: bricks
            new MaterialCombination(5, 3, 2), // roof: crimson, stair: polished granite
            // wall: end stone brick (4), stair: bricks/polished granite
            new MaterialCombination(0, 4, 0), // roof: spruce, stair: bricks
            new MaterialCombination(0, 4, 2), // roof: spruce, stair: polished granite
            new MaterialCombination(1, 4, 0), // roof: dark oak, stair: bricks
            new MaterialCombination(1, 4, 2), // roof: dark oak, stair: polished granite
            new MaterialCombination(2, 4, 0), // roof: deepslate bricks, stair: bricks
            new MaterialCombination(2, 4, 2), // roof: deepslate bricks, stair: polished granite
            new MaterialCombination(3, 4, 0), // roof: dark prismarine, stair: bricks
            new MaterialCombination(3, 4, 2), // roof: dark prismarine, stair: polished granite
            new MaterialCombination(4, 4, 0), // roof: red nether bricks, stair: bricks
            new MaterialCombination(4, 4, 2), // roof: red nether bricks, stair: polished granite
            new MaterialCombination(5, 4, 0), // roof: crimson, stair: bricks
            new MaterialCombination(5, 4, 2)  // roof: crimson, stair: polished granite
    );

    public static int combinationCount() {
        return ALLOWED_COMBINATIONS.size();
    }

    public static MaterialCombination get(int index) {
        return ALLOWED_COMBINATIONS.get(index);
    }
}
