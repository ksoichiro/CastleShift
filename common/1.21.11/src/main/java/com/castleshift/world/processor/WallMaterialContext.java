package com.castleshift.world.processor;

public class WallMaterialContext {
    private static final ThreadLocal<Integer> MATERIAL_INDEX = new ThreadLocal<>();

    public static void set(int index) {
        MATERIAL_INDEX.set(index);
    }

    public static int get() {
        Integer index = MATERIAL_INDEX.get();
        return index != null ? index : 0;
    }

    public static void clear() {
        MATERIAL_INDEX.remove();
    }
}
