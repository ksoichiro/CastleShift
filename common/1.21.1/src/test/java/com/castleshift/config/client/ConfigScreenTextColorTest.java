package com.castleshift.config.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * 1.21.1's {@code EditBox} draws the {@code setHint(Component)} placeholder with the same color as
 * real input, so the hint only reads as a placeholder while the box's text color is dimmed. These
 * pin the three states apart.
 */
class ConfigScreenTextColorTest {

    @Test
    void emptyFieldUsesTheDimHintColor() {
        int empty = ConfigScreen.textColorFor("", false);

        assertNotEquals(ConfigScreen.textColorFor("32", false), empty);
        assertNotEquals(ConfigScreen.textColorFor("32", true), empty);
    }

    @Test
    void nullFieldIsTreatedAsEmpty() {
        assertEquals(ConfigScreen.textColorFor("", false), ConfigScreen.textColorFor(null, false));
    }

    @Test
    void typedValueIsNormalAndInvalidValueIsDistinct() {
        assertNotEquals(ConfigScreen.textColorFor("32", true), ConfigScreen.textColorFor("32", false));
    }
}
