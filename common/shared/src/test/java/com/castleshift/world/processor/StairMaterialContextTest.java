package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class StairMaterialContextTest {

    @AfterEach
    void cleanup() {
        StairMaterialContext.clear();
    }

    @Test
    void defaultValueIsZero() {
        assertEquals(0, StairMaterialContext.get());
    }

    @Test
    void setAndGetReturnsSameValue() {
        StairMaterialContext.set(6);
        assertEquals(6, StairMaterialContext.get());
    }

    @Test
    void clearResetsToDefault() {
        StairMaterialContext.set(3);
        StairMaterialContext.clear();
        assertEquals(0, StairMaterialContext.get());
    }
}
