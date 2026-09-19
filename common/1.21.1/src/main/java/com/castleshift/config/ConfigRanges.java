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

    /**
     * Returns a {@link CastleShiftConfig.Generation} whose <em>effective</em> spacing/separation are
     * guaranteed to be in range and to satisfy {@code separation < spacing}.
     *
     * <p>Single source of truth for the invariant: both the file-read path
     * ({@code ConfigLoader#load}) and the config screen go through this, so a value accepted by the
     * screen survives a save/restart round trip unchanged.
     *
     * <p>Clamping each override on its own is not enough, because an override on one side is
     * combined with the preset's value on the other (e.g. {@code custom_spacing = 8} with the
     * {@code SPARSE} preset's separation of 16). The correction is therefore always applied to
     * {@code customSeparation}: spacing is what the user asked for, and separation is derived down
     * to fit under it. A {@code null} override stays {@code null} unless it has to be materialised
     * to hold the corrected value.
     */
    public static CastleShiftConfig.Generation resolveEffective(CastleShiftConfig.Generation generation) {
        int spacing = clampSpacing(generation.effectiveSpacing());
        int separation = clampSeparation(spacing, generation.effectiveSeparation());

        Integer customSpacing = generation.customSpacing() == null ? null : spacing;
        Integer customSeparation;
        if (generation.customSeparation() != null) {
            customSeparation = separation;
        } else {
            // Preset separation had to be corrected to fit under the custom spacing, so it can no
            // longer be left implicit.
            customSeparation = separation == generation.effectiveSeparation() ? null : separation;
        }

        return new CastleShiftConfig.Generation(
                generation.enabled(), generation.preset(), customSpacing, customSeparation);
    }
}
