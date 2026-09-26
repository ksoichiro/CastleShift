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
    void presetButtonIsActiveOnlyWhenEnabledAndNoOverride() {
        // Enabled: active only when neither field is overridden. Overriding either one locks the
        // button, since it would otherwise look adjustable while only being able to affect the
        // still-preset-controlled field.
        assertTrue(ConfigScreen.presetButtonActive(true, false, false));
        assertFalse(ConfigScreen.presetButtonActive(true, true, false));
        assertFalse(ConfigScreen.presetButtonActive(true, false, true));
        assertFalse(ConfigScreen.presetButtonActive(true, true, true));

        // Disabled ("Generate Castles" off): inactive regardless of the override state, since
        // nothing the preset controls has any effect while generation itself is off.
        assertFalse(ConfigScreen.presetButtonActive(false, false, false));
        assertFalse(ConfigScreen.presetButtonActive(false, true, true));
    }

    @Test
    void invalidCustomFieldsOnlyBlockDoneWhileGenerationIsEnabled() {
        // Unchanged existing behavior: an invalid value blocks Done while generation is on.
        assertTrue(ConfigScreen.fieldsInvalid(true, true, false));
        assertTrue(ConfigScreen.fieldsInvalid(true, false, true));
        assertTrue(ConfigScreen.fieldsInvalid(true, true, true));
        assertFalse(ConfigScreen.fieldsInvalid(true, false, false));

        // The fix: once "Generate Castles" is off, both custom boxes are disabled and have no
        // effect on worldgen, so a stale invalid value left in one of them must not trap the
        // player on this screen (previously Done stayed disabled with no visible explanation,
        // since the disabled box also suppresses the red error-text color).
        assertFalse(ConfigScreen.fieldsInvalid(false, true, false));
        assertFalse(ConfigScreen.fieldsInvalid(false, false, true));
        assertFalse(ConfigScreen.fieldsInvalid(false, true, true));
        assertFalse(ConfigScreen.fieldsInvalid(false, false, false));
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
