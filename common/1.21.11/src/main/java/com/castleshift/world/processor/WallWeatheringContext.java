package com.castleshift.world.processor;

public class WallWeatheringContext {
    private static final ThreadLocal<Boolean> ENABLED = new ThreadLocal<>();

    public static void set(boolean enabled) {
        ENABLED.set(enabled);
    }

    public static boolean isEnabled() {
        Boolean enabled = ENABLED.get();
        return enabled != null && enabled;
    }

    public static void clear() {
        ENABLED.remove();
    }
}
