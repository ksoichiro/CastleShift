package com.castleshift.config;

public record CastleShiftConfig(Generation generation) {

    private static volatile CastleShiftConfig instance = ConfigDefaults.defaults();

    public static CastleShiftConfig get() {
        return instance;
    }

    public static void set(CastleShiftConfig config) {
        instance = config;
    }

    public enum Preset {
        SPARSE,
        DEFAULT,
        FREQUENT
    }

    public record Generation(boolean enabled, Preset preset, Integer customSpacing, Integer customSeparation) {

        public int effectiveSpacing() {
            if (customSpacing != null) {
                return customSpacing;
            }
            return switch (preset) {
                case SPARSE -> ConfigDefaults.SPARSE_SPACING;
                case DEFAULT -> ConfigDefaults.DEFAULT_SPACING;
                case FREQUENT -> ConfigDefaults.FREQUENT_SPACING;
            };
        }

        public int effectiveSeparation() {
            if (customSeparation != null) {
                return customSeparation;
            }
            return switch (preset) {
                case SPARSE -> ConfigDefaults.SPARSE_SEPARATION;
                case DEFAULT -> ConfigDefaults.DEFAULT_SEPARATION;
                case FREQUENT -> ConfigDefaults.FREQUENT_SEPARATION;
            };
        }
    }
}
