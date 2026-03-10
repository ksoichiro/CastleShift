package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class WallWeatheringContextTest {

    @AfterEach
    void cleanup() {
        WallWeatheringContext.clear();
    }

    @Test
    void defaultValueIsFalse() {
        assertFalse(WallWeatheringContext.isEnabled());
    }

    @Test
    void setTrueAndGetReturnsTrue() {
        WallWeatheringContext.set(true);
        assertTrue(WallWeatheringContext.isEnabled());
    }

    @Test
    void setFalseAndGetReturnsFalse() {
        WallWeatheringContext.set(true);
        WallWeatheringContext.set(false);
        assertFalse(WallWeatheringContext.isEnabled());
    }

    @Test
    void clearResetsToDefault() {
        WallWeatheringContext.set(true);
        WallWeatheringContext.clear();
        assertFalse(WallWeatheringContext.isEnabled());
    }
}
