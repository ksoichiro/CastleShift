package com.castleshift.world.processor;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for tests that require Minecraft bootstrap.
 * Initializes block registry, block states, etc.
 */
abstract class McBootstrapTestBase {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}
