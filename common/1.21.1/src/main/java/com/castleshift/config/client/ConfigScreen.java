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

    public ConfigScreen(Screen parent, Path configFile) {
        super(Component.translatable("config.castleshift.title"));
        this.parent = parent;
        this.configFile = configFile;
        CastleShiftConfig.Generation gen = ConfigLoader.load(configFile).generation();
        this.enabled = gen.enabled();
        this.preset = gen.preset();
        this.initialCustomSpacing = gen.customSpacing();
        this.initialCustomSeparation = gen.customSeparation();
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
                        (button, value) -> this.preset = value));

        y += 24;
        this.customSpacingBox = new EditBox(this.font, centerX - 150, y, 145, 20,
                Component.translatable("config.castleshift.option.custom_spacing"));
        this.customSpacingBox.setResponder(text -> this.validate());
        if (this.initialCustomSpacing != null) {
            this.customSpacingBox.setValue(String.valueOf(this.initialCustomSpacing));
        }
        this.addRenderableWidget(this.customSpacingBox);

        this.customSeparationBox = new EditBox(this.font, centerX + 5, y, 145, 20,
                Component.translatable("config.castleshift.option.custom_separation"));
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
        this.fieldsInvalid = false;
        Integer spacing = parseNullableInt(this.customSpacingBox.getValue());
        Integer separation = parseNullableInt(this.customSeparationBox.getValue());
        if (spacing != null && (spacing < ConfigRanges.MIN_SPACING || spacing > ConfigRanges.MAX_SPACING)) {
            this.fieldsInvalid = true;
        }
        if (separation != null && (separation < ConfigRanges.MIN_SEPARATION || separation > ConfigRanges.MAX_SEPARATION)) {
            this.fieldsInvalid = true;
        }
        if (spacing != null && separation != null && separation >= spacing) {
            this.fieldsInvalid = true;
        }
        if (this.doneButton != null) {
            this.doneButton.active = !this.fieldsInvalid;
        }
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
        CastleShiftConfig updated = new CastleShiftConfig(
                new CastleShiftConfig.Generation(this.enabled, this.preset, customSpacing, customSeparation));
        ConfigWriter.save(this.configFile, updated);
        CastleShiftConfig.set(updated);
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
