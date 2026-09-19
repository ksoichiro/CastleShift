package com.castleshift.config;

public final class ConfigRanges {
    public static final int MIN_SPACING = 8;
    public static final int MAX_SPACING = 512;
    public static final int MIN_SEPARATION = 1;
    public static final int MAX_SEPARATION = 256;

    private ConfigRanges() {}

    public static int clampSpacing(int spacing) {
        return Math.max(MIN_SPACING, Math.min(MAX_SPACING, spacing));
    }

    public static int clampSeparation(int spacing, int separation) {
        int bounded = Math.max(MIN_SEPARATION, Math.min(MAX_SEPARATION, separation));
        return Math.min(bounded, spacing - 1);
    }
}
