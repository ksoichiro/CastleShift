package com.castleshift.world.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RoofMaterialContextTest {

    @AfterEach
    void cleanup() {
        RoofMaterialContext.clear();
    }

    @Test
    void defaultValueIsZero() {
        assertEquals(0, RoofMaterialContext.get());
    }

    @Test
    void setAndGetReturnsSameValue() {
        RoofMaterialContext.set(3);
        assertEquals(3, RoofMaterialContext.get());
    }

    @Test
    void clearResetsToDefault() {
        RoofMaterialContext.set(5);
        RoofMaterialContext.clear();
        assertEquals(0, RoofMaterialContext.get());
    }
}
