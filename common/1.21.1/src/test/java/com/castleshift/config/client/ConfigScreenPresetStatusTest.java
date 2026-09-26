package com.castleshift.config.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * The status line under the custom boxes has to say, per field, whether the preset or the typed
 * value is in effect, because a custom value always wins over the preset for that one field.
 */
class ConfigScreenPresetStatusTest {

    @Test
    void eachOverrideCombinationHasItsOwnStatus() {
        assertEquals("config.castleshift.status.preset_sets_both", ConfigScreen.presetStatusKey(false, false));
        assertEquals("config.castleshift.status.preset_sets_separation", ConfigScreen.presetStatusKey(true, false));
        assertEquals("config.castleshift.status.preset_sets_spacing", ConfigScreen.presetStatusKey(false, true));
        assertEquals("config.castleshift.status.preset_unused", ConfigScreen.presetStatusKey(true, true));
    }

    @Test
    void presetButtonIsActiveOnlyWhenEnabledAndNotFullyOverridden() {
        // Enabled: active unless both fields are overridden.
        assertTrue(ConfigScreen.presetButtonActive(true, false, false));
        assertTrue(ConfigScreen.presetButtonActive(true, true, false));
        assertTrue(ConfigScreen.presetButtonActive(true, false, true));
        assertFalse(ConfigScreen.presetButtonActive(true, true, true));

        // Disabled ("Generate Castles" off): inactive regardless of the override state, since
        // nothing the preset controls has any effect while generation itself is off.
        assertFalse(ConfigScreen.presetButtonActive(false, false, false));
        assertFalse(ConfigScreen.presetButtonActive(false, true, true));
    }

    @Test
    void everyStatusKeyAndTheHintAreTranslatedInEnglishAndJapanese() throws Exception {
        String[] keys = {
            ConfigScreen.presetStatusKey(false, false),
            ConfigScreen.presetStatusKey(true, false),
            ConfigScreen.presetStatusKey(false, true),
            ConfigScreen.presetStatusKey(true, true),
            "config.castleshift.hint.custom_overrides_preset",
        };
        for (String lang : new String[] {"en_us", "ja_jp"}) {
            JsonObject json = readLang(lang);
            for (String key : keys) {
                assertTrue(json.has(key), lang + " is missing " + key);
            }
        }
    }

    private static JsonObject readLang(String lang) throws Exception {
        String path = "/assets/castleshift/lang/" + lang + ".json";
        try (InputStream in = ConfigScreenPresetStatusTest.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("missing resource " + path);
            }
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
