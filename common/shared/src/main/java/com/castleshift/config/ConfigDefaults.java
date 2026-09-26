package com.castleshift.config;

public final class ConfigDefaults {
    public static final int SPARSE_SPACING = 64;
    public static final int SPARSE_SEPARATION = 16;
    public static final int DEFAULT_SPACING = 32;
    public static final int DEFAULT_SEPARATION = 8;
    public static final int FREQUENT_SPACING = 16;
    public static final int FREQUENT_SEPARATION = 4;
    public static final int CURRENT_SCHEMA_VERSION = 1;

    private ConfigDefaults() {}

    public static CastleShiftConfig defaults() {
        return new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, null, null));
    }
}
