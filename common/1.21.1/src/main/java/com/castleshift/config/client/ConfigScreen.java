package com.castleshift.config.client;

import com.castleshift.config.CastleShiftConfig;
import com.castleshift.config.ConfigDefaults;
import com.castleshift.config.ConfigLoader;
import com.castleshift.config.ConfigRanges;
import com.castleshift.config.ConfigWriter;
import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final Path configFile;

    private boolean enabled;
    private CastleShiftConfig.Preset preset;
    private Integer initialCustomSpacing;
    private Integer initialCustomSeparation;
    private EditBox customSpacingBox;
    private EditBox customSeparationBox;
    private CycleButton<Boolean> enabledButton;
    private CycleButton<CastleShiftConfig.Preset> presetButton;
    private Button doneButton;
    private boolean fieldsInvalid;
    private boolean showNonAuthoritativeWarning;
    private boolean presetFullyOverridden;
    private Component presetStatus = Component.empty();
    private int presetStatusColor;
    private int overrideHintY;

    /** Vanilla's error red (same tone as {@code ChatFormatting.RED}'s lighter UI variant). */
    private static final int INVALID_TEXT_COLOR = 0xFFFF5555;

    /** {@code EditBox}'s own default text color (14737632). */
    private static final int VALID_TEXT_COLOR = 0xFFE0E0E0;

    /**
     * The gray vanilla hardcodes for {@code EditBox}'s suggestion text (-8355712).
     *
     * <p>Needed because 1.21.1's {@code EditBox#renderWidget} draws the {@code setHint(Component)}
     * placeholder with the very same color as real input: it reuses the local
     * {@code isEditable ? textColor : textColorUneditable} value for both, unlike the older
     * {@code setSuggestion(String)} path, which dims itself. So the hint only looks like a
     * placeholder if {@code setTextColor} is dimmed while the box is empty.
     */
    private static final int HINT_TEXT_COLOR = 0xFF808080;

    /** Help/status text while the preset still controls both values. */
    private static final int PRESET_STATUS_NEUTRAL_COLOR = 0xFFA0A0A0;

    /** Status text once a custom value has taken over from the preset. */
    private static final int PRESET_STATUS_OVERRIDDEN_COLOR = 0xFFFFD37F;

    public ConfigScreen(Screen parent, Path configFile) {
        super(Component.translatable("config.castleshift.title"));
        this.parent = parent;
        this.configFile = configFile;
        CastleShiftConfig.Generation gen = loadOrActive(configFile);
        this.enabled = gen.enabled();
        this.preset = gen.preset();
        this.initialCustomSpacing = gen.customSpacing();
        this.initialCustomSeparation = gen.customSeparation();
    }

    /**
     * Reads the config file, falling back to the currently active in-memory config if it cannot be
     * parsed.
     *
     * <p>This screen is the only UI that can repair a broken {@code castleshift.toml}, so it must
     * open rather than throw out of its constructor (which would crash the game via
     * {@code Minecraft#setScreen}). {@link CastleShiftConfig#get()} is always valid: mod init
     * already falls back to the defaults when the file fails to load.
     */
    private static CastleShiftConfig.Generation loadOrActive(Path configFile) {
        try {
            return ConfigLoader.load(configFile).generation();
        } catch (RuntimeException e) {
            System.err.println("[castleshift] failed to load " + configFile
                    + " for the config screen; showing the active config instead: " + e);
            return CastleShiftConfig.get().generation();
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 4;

        this.showNonAuthoritativeWarning =
                this.minecraft.level != null && !this.minecraft.hasSingleplayerServer();
        if (this.showNonAuthoritativeWarning) {
            // Non-authoritative warning: worldgen is server-side, so edits here only
            // affect this client's local copy, not the joined server's actual settings.
            y += 20;
        }

        this.enabledButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.enabled)
                .create(centerX - 150, y, 300, 20, Component.translatable("config.castleshift.option.enabled"),
                        (button, value) -> this.enabled = value));

        y += 24;
        this.presetButton = this.addRenderableWidget(CycleButton.<CastleShiftConfig.Preset>builder(
                        this::presetValueLabel)
                .withValues(CastleShiftConfig.Preset.values())
                .withInitialValue(this.preset)
                .create(centerX - 150, y, 300, 20, Component.translatable("config.castleshift.option.preset"),
                        (button, value) -> {
                            this.preset = value;
                            // The preset supplies whichever of spacing/separation is not overridden,
                            // so the cross-field check has to run again when it changes.
                            this.validate();
                        }));

        y += 24;
        // Both boxes are constructed and pre-populated before either gets a responder attached.
        // validate() reads both customSpacingBox and customSeparationBox, so wiring a responder
        // (or calling setValue(), which fires it) before the second box exists is a null
        // dereference the moment a saved override makes setValue() non-blank during init().
        this.customSpacingBox = new EditBox(this.font, centerX - 150, y, 145, 20,
                Component.translatable("config.castleshift.option.custom_spacing"));
        // The constructor Component is narration-only; the hint is what the player actually sees.
        this.customSpacingBox.setHint(Component.translatable("config.castleshift.option.custom_spacing"));

        this.customSeparationBox = new EditBox(this.font, centerX + 5, y, 145, 20,
                Component.translatable("config.castleshift.option.custom_separation"));
        this.customSeparationBox.setHint(Component.translatable("config.castleshift.option.custom_separation"));

        if (this.initialCustomSpacing != null) {
            this.customSpacingBox.setValue(String.valueOf(this.initialCustomSpacing));
        }
        if (this.initialCustomSeparation != null) {
            this.customSeparationBox.setValue(String.valueOf(this.initialCustomSeparation));
        }

        this.customSpacingBox.setResponder(text -> this.validate());
        this.customSeparationBox.setResponder(text -> this.validate());

        this.addRenderableWidget(this.customSpacingBox);
        this.addRenderableWidget(this.customSeparationBox);

        // Two text lines (static precedence hint, then the live status from validate()) drawn in
        // render(), between the custom boxes and the bottom buttons.
        y += 26;
        this.overrideHintY = y;

        y += 30;
        this.addRenderableWidget(Button.builder(Component.translatable("config.castleshift.reset"),
                        button -> this.resetToDefaults())
                .bounds(centerX - 150, y, 95, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),
                        button -> this.minecraft.setScreen(this.parent))
                .bounds(centerX - 50, y, 95, 20)
                .build());
        this.doneButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        button -> this.saveAndClose())
                .bounds(centerX + 55, y, 95, 20)
                .build());

        this.validate();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String spacingText = this.customSpacingBox == null ? "" : this.customSpacingBox.getValue();
        String separationText = this.customSeparationBox == null ? "" : this.customSeparationBox.getValue();
        super.resize(minecraft, width, height);
        this.customSpacingBox.setValue(spacingText);
        this.customSeparationBox.setValue(separationText);
        this.validate();
    }

    private void validate() {
        String spacingText = this.customSpacingBox.getValue();
        String separationText = this.customSeparationBox.getValue();
        Integer spacing = parseNullableInt(spacingText);
        Integer separation = parseNullableInt(separationText);

        // Blank means "no override" and is valid; non-blank text that does not parse is a typo and
        // must be reported instead of being silently dropped as if the box were empty.
        boolean spacingInvalid = isUnparseable(spacingText)
                || (spacing != null && (spacing < ConfigRanges.MIN_SPACING || spacing > ConfigRanges.MAX_SPACING));
        boolean separationInvalid = isUnparseable(separationText)
                || (separation != null
                        && (separation < ConfigRanges.MIN_SEPARATION || separation > ConfigRanges.MAX_SEPARATION));

        if (!spacingInvalid && !separationInvalid) {
            // Cross-field check against the same invariant ConfigRanges enforces on the read path,
            // including the one-sided case where the other value comes from the preset.
            CastleShiftConfig.Generation candidate =
                    new CastleShiftConfig.Generation(this.enabled, this.preset, spacing, separation);
            if (candidate.effectiveSeparation() >= candidate.effectiveSpacing()) {
                spacingInvalid = spacing != null;
                separationInvalid = separation != null;
            }
        }

        this.customSpacingBox.setTextColor(textColorFor(spacingText, spacingInvalid));
        this.customSeparationBox.setTextColor(textColorFor(separationText, separationInvalid));

        this.fieldsInvalid = spacingInvalid || separationInvalid;
        if (this.doneButton != null) {
            this.doneButton.active = !this.fieldsInvalid;
        }

        this.updatePresetStatus(!spacingText.isBlank(), !separationText.isBlank());
    }

    /**
     * Makes the per-field precedence visible: a filled-in custom box always wins over the preset
     * for that one value, so picking a preset can silently do nothing for an overridden field.
     * Any non-blank text counts as an override, even while it is still invalid, because that is
     * the value that will be used (or block Done) once it is fixed.
     */
    private void updatePresetStatus(boolean spacingOverridden, boolean separationOverridden) {
        CastleShiftConfig.Generation fromPreset =
                new CastleShiftConfig.Generation(this.enabled, this.preset, null, null);
        String key = presetStatusKey(spacingOverridden, separationOverridden);
        this.presetStatus = Component.translatable(
                key, fromPreset.effectiveSpacing(), fromPreset.effectiveSeparation());
        this.presetStatusColor = spacingOverridden || separationOverridden
                ? PRESET_STATUS_OVERRIDDEN_COLOR
                : PRESET_STATUS_NEUTRAL_COLOR;

        boolean fullyOverridden = spacingOverridden && separationOverridden;
        if (this.presetButton != null && fullyOverridden != this.presetFullyOverridden) {
            this.presetFullyOverridden = fullyOverridden;
            // setValue() re-renders the label through presetValueLabel without firing the
            // onValueChange callback, so this cannot recurse into validate().
            this.presetButton.setValue(this.preset);
        }
    }

    /**
     * Lang key for the status line. Every key takes the preset's spacing and separation as its two
     * arguments (in that order), even the ones that show only one or none of them.
     */
    static String presetStatusKey(boolean spacingOverridden, boolean separationOverridden) {
        if (spacingOverridden && separationOverridden) {
            return "config.castleshift.status.preset_unused";
        }
        if (spacingOverridden) {
            return "config.castleshift.status.preset_sets_separation";
        }
        if (separationOverridden) {
            return "config.castleshift.status.preset_sets_spacing";
        }
        return "config.castleshift.status.preset_sets_both";
    }

    /**
     * Grays out the preset's value while both values are overridden. The button stays clickable on
     * purpose: the player may be about to clear an override and want the preset ready for it.
     */
    private Component presetValueLabel(CastleShiftConfig.Preset p) {
        Component label = Component.translatable("config.castleshift.preset." + p.name().toLowerCase());
        return this.presetFullyOverridden ? label.copy().withStyle(ChatFormatting.DARK_GRAY) : label;
    }

    /**
     * Three visual states share the one {@code EditBox} text color: the hint (empty box), a normal
     * typed value, and an out-of-range/unparseable one. An empty box is never invalid (blank means
     * "no override"), so the states cannot collide.
     */
    static int textColorFor(String text, boolean invalid) {
        if (text == null || text.isEmpty()) {
            return HINT_TEXT_COLOR;
        }
        return invalid ? INVALID_TEXT_COLOR : VALID_TEXT_COLOR;
    }

    private static boolean isUnparseable(String text) {
        return text != null && !text.isBlank() && parseNullableInt(text) == null;
    }

    private static Integer parseNullableInt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void resetToDefaults() {
        CastleShiftConfig.Generation defaults = ConfigDefaults.defaults().generation();
        this.enabled = defaults.enabled();
        this.preset = defaults.preset();
        this.initialCustomSpacing = defaults.customSpacing();
        this.initialCustomSeparation = defaults.customSeparation();
        // CycleButton keeps its own selection, and setValue() deliberately does not fire the
        // onValueChange callback, so the widget and the field above have to be updated in pairs;
        // assigning only the field leaves the button still showing the old choice.
        this.enabledButton.setValue(this.enabled);
        this.presetButton.setValue(this.preset);
        this.customSpacingBox.setValue("");
        this.customSeparationBox.setValue("");
        this.validate();
    }

    private void saveAndClose() {
        Integer customSpacing = parseNullableInt(this.customSpacingBox.getValue());
        Integer customSeparation = parseNullableInt(this.customSeparationBox.getValue());
        // Go through the same clamping the loader applies, so what is written here is exactly what
        // would be read back after a restart.
        CastleShiftConfig updated = new CastleShiftConfig(ConfigRanges.resolveEffective(
                new CastleShiftConfig.Generation(this.enabled, this.preset, customSpacing, customSeparation)));
        ConfigWriter.save(this.configFile, updated);
        CastleShiftConfig.set(updated);
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void onClose() {
        // Match Cancel: Escape returns to whoever opened this screen (the mods list, or the game
        // when opened via the keybind), instead of Screen's default setScreen(null).
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        if (this.showNonAuthoritativeWarning) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("config.castleshift.warning.not_authoritative"),
                    this.width / 2, 15 + this.font.lineHeight + 5, 0xFFFF55);
        }
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("config.castleshift.hint.custom_overrides_preset"),
                this.width / 2, this.overrideHintY, PRESET_STATUS_NEUTRAL_COLOR);
        guiGraphics.drawCenteredString(this.font, this.presetStatus,
                this.width / 2, this.overrideHintY + this.font.lineHeight + 3, this.presetStatusColor);
    }
}
