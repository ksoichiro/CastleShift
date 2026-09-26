package com.castleshift.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigWriterTest {

    @TempDir Path tempDir;

    @Test
    void writesValuesAndPreservesLeadingComment() throws Exception {
        Path configFile = tempDir.resolve("castleshift.toml");
        ConfigLoader.load(configFile); // seeds the file with the bundled default (incl. its header comment)

        CastleShiftConfig updated = new CastleShiftConfig(
                new CastleShiftConfig.Generation(false, CastleShiftConfig.Preset.FREQUENT, 20, 3));
        ConfigWriter.save(configFile, updated);

        CastleShiftConfig reloaded = ConfigLoader.load(configFile);
        assertEquals(updated, reloaded);

        String contents = Files.readString(configFile);
        assertTrue(contents.contains("Castle Shift generation settings"));
    }
}
