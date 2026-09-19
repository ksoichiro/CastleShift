package com.castleshift.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import java.nio.file.Path;

public final class ConfigWriter {

    private ConfigWriter() {}

    public static void save(Path configFile, CastleShiftConfig config) {
        try (CommentedFileConfig fileConfig =
                CommentedFileConfig.builder(configFile, TomlFormat.instance()).sync().build()) {
            try {
                fileConfig.load();
            } catch (RuntimeException e) {
                // The existing file is read only to preserve keys this mod does not own. If it is
                // unparseable (hand-edited), saving from the config screen must still work, so
                // start from an empty config and write the full set of values below.
                System.err.println("[castleshift] could not parse " + configFile
                        + " before saving; rewriting it from scratch: " + e);
                fileConfig.clear();
            }

            CastleShiftConfig.Generation gen = config.generation();
            fileConfig.set("schema_version", ConfigDefaults.CURRENT_SCHEMA_VERSION);
            fileConfig.set("generation.enabled", gen.enabled());
            fileConfig.set("generation.preset", gen.preset().name());
            if (gen.customSpacing() != null) {
                fileConfig.set("generation.custom_spacing", gen.customSpacing());
            } else {
                fileConfig.remove("generation.custom_spacing");
            }
            if (gen.customSeparation() != null) {
                fileConfig.set("generation.custom_separation", gen.customSeparation());
            } else {
                fileConfig.remove("generation.custom_separation");
            }

            fileConfig.save();
        }
    }
}
