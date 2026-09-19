package com.castleshift.config.client;

import com.castleshift.config.CastleShiftConfig;
import com.castleshift.config.ConfigDefaults;
import com.castleshift.config.ConfigLoader;
import com.castleshift.config.ConfigRanges;
import com.castleshift.config.ConfigWriter;
import java.nio.file.Path;
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
    private Button doneButton;
    private boolean fieldsInvalid;
    private boolean showNonAuthoritativeWarning;

    /** Vanilla's error red (same tone as {@code ChatFormatting.RED}'s lighter UI variant). */
    private static final int INVALID_TEXT_COLOR = 0xFFFF5555;

    /** {@code EditBox}'s default text color. */
    private static final int VALID_TEXT_COLOR = 0xFFFFFFFF;

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

        this.addRenderableWidget(CycleButton.onOffBuilder(this.enabled)
                .create(centerX - 150, y, 300, 20, Component.translatable("config.castleshift.option.enabled"),
                        (button, value) -> this.enabled = value));

        y += 24;
        this.addRenderableWidget(CycleButton.<CastleShiftConfig.Preset>builder(p ->
                        Component.translatable("config.castleshift.preset." + p.name().toLowerCase()))
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
        this.customSpacingBox = new EditBox(this.font, centerX - 150, y, 145, 20,
                Component.translatable("config.castleshift.option.custom_spacing"));
        // The constructor Component is narration-only; the hint is what the player actually sees.
        this.customSpacingBox.setHint(Component.translatable("config.castleshift.option.custom_spacing"));
        this.customSpacingBox.setResponder(text -> this.validate());
        if (this.initialCustomSpacing != null) {
            this.customSpacingBox.setValue(String.valueOf(this.initialCustomSpacing));
        }
        this.addRenderableWidget(this.customSpacingBox);

        this.customSeparationBox = new EditBox(this.font, centerX + 5, y, 145, 20,
                Component.translatable("config.castleshift.option.custom_separation"));
        this.customSeparationBox.setHint(Component.translatable("config.castleshift.option.custom_separation"));
        this.customSeparationBox.setResponder(text -> this.validate());
        if (this.initialCustomSeparation != null) {
            this.customSeparationBox.setValue(String.valueOf(this.initialCustomSeparation));
        }
        this.addRenderableWidget(this.customSeparationBox);

        y += 32;
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

        this.customSpacingBox.setTextColor(spacingInvalid ? INVALID_TEXT_COLOR : VALID_TEXT_COLOR);
        this.customSeparationBox.setTextColor(separationInvalid ? INVALID_TEXT_COLOR : VALID_TEXT_COLOR);

        this.fieldsInvalid = spacingInvalid || separationInvalid;
        if (this.doneButton != null) {
            this.doneButton.active = !this.fieldsInvalid;
        }
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
    }
}
