package com.castleshift.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigLoader {

    /** Name of the config file, relative to each loader's config directory. */
    public static final String CONFIG_FILE_NAME = "castleshift.toml";

    private ConfigLoader() {}

    /**
     * Loads {@code configFile} and makes it authoritative for world generation.
     *
     * <p>Called from every loader's common mod-init path so a hand-edited config file takes effect
     * on a dedicated server, and on any client session where the config screen is never opened.
     * A broken file must not prevent the game from starting, so failures fall back to the defaults
     * already held by {@link CastleShiftConfig}.
     */
    public static void loadAndApply(Path configFile) {
        try {
            CastleShiftConfig.set(load(configFile));
        } catch (RuntimeException e) {
            System.err.println(
                    "[castleshift] failed to load " + configFile + "; keeping defaults: " + e);
        }
    }

    public static CastleShiftConfig load(Path configFile) {
        if (!Files.exists(configFile)) {
            copyBundledDefault(configFile);
        }

        try (CommentedFileConfig fileConfig = CommentedFileConfig.of(configFile)) {
            fileConfig.load();

            int schemaVersion = fileConfig.getIntOrElse("schema_version", ConfigDefaults.CURRENT_SCHEMA_VERSION);
            if (schemaVersion != ConfigDefaults.CURRENT_SCHEMA_VERSION) {
                System.err.println(
                        "[castleshift] config schema_version " + schemaVersion
                                + " does not match expected " + ConfigDefaults.CURRENT_SCHEMA_VERSION
                                + "; missing fields will use defaults.");
            }

            CastleShiftConfig.Generation defaultsGen = ConfigDefaults.defaults().generation();

            boolean enabled = fileConfig.getOrElse("generation.enabled", defaultsGen.enabled());

            CastleShiftConfig.Preset preset = parsePreset(
                    fileConfig.getOrElse("generation.preset", defaultsGen.preset().name()), defaultsGen.preset());

            Integer customSpacing = readNullableInt(fileConfig, "generation.custom_spacing");
            Integer customSeparation = readNullableInt(fileConfig, "generation.custom_separation");

            if (customSpacing != null) {
                customSpacing = ConfigRanges.clampSpacing(customSpacing);
            }
            if (customSeparation != null) {
                int spacingForClamp = customSpacing != null ? customSpacing : new CastleShiftConfig.Generation(
                                enabled, preset, null, null)
                        .effectiveSpacing();
                customSeparation = ConfigRanges.clampSeparation(spacingForClamp, customSeparation);
            }

            return new CastleShiftConfig(
                    new CastleShiftConfig.Generation(enabled, preset, customSpacing, customSeparation));
        }
    }

    private static CastleShiftConfig.Preset parsePreset(String raw, CastleShiftConfig.Preset fallback) {
        try {
            return CastleShiftConfig.Preset.valueOf(raw);
        } catch (IllegalArgumentException e) {
            System.err.println("[castleshift] invalid generation.preset '" + raw + "'; falling back to " + fallback);
            return fallback;
        }
    }

    private static Integer readNullableInt(FileConfig fileConfig, String path) {
        Number value = fileConfig.get(path);
        return value == null ? null : value.intValue();
    }

    private static void copyBundledDefault(Path configFile) {
        try {
            Files.createDirectories(configFile.getParent());
            try (InputStream in = ConfigLoader.class.getResourceAsStream("/castleshift.toml")) {
                if (in == null) {
                    throw new IllegalStateException("Bundled castleshift.toml resource is missing");
                }
                Files.copy(in, configFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
