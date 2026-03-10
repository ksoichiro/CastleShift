package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class WallMaterialContextTest {

    @AfterEach
    void cleanup() {
        WallMaterialContext.clear();
    }

    @Test
    void defaultValueIsZero() {
        assertEquals(0, WallMaterialContext.get());
    }

    @Test
    void setAndGetReturnsSameValue() {
        WallMaterialContext.set(2);
        assertEquals(2, WallMaterialContext.get());
    }

    @Test
    void clearResetsToDefault() {
        WallMaterialContext.set(4);
        WallMaterialContext.clear();
        assertEquals(0, WallMaterialContext.get());
    }
}
