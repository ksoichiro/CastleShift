package com.castleshift.fabric;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.castleshift.config.CastleShiftConfig;
import com.castleshift.config.ConfigDefaults;
import com.castleshift.world.placement.ConfigurableSpreadStructurePlacement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Runs under fabric-loader-junit, so Minecraft classes are loaded through Knot with this mod's
 * mixins applied. That is the only reason this test lives in the Fabric project: the tests in
 * common-1.21.1 run on a plain JVM and never see ChunkGeneratorMixin at all.
 *
 * <p>Guards the {@code enabled = false} /locate stall (about 254 s of frozen server thread and
 * 40,401 region files on a fresh world before the mixin). The vanilla ring search is called with a
 * null level and structure manager: if the mixin is applied it returns before touching either,
 * and if it is missing or no longer matches, vanilla dereferences them and throws.
 */
class ChunkGeneratorMixinTest {

    private static Method getNearestGeneratedStructure;

    @BeforeAll
    static void setUp() throws NoSuchMethodException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        getNearestGeneratedStructure = ChunkGenerator.class.getDeclaredMethod(
                "getNearestGeneratedStructure",
                Set.class,
                LevelReader.class,
                StructureManager.class,
                int.class,
                int.class,
                int.class,
                boolean.class,
                long.class,
                RandomSpreadStructurePlacement.class);
        getNearestGeneratedStructure.setAccessible(true);
    }

    @AfterEach
    void resetConfig() {
        CastleShiftConfig.set(ConfigDefaults.defaults());
    }

    @Test
    void ringSearchIsSkippedWhileGenerationIsDisabled() {
        setEnabled(false);

        // Ring 100 is the radius /locate uses. Without the mixin this is the multi-minute search.
        Object result = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> invokeRingSearch(100));

        assertNull(result);
    }

    /**
     * Control for the test above: with generation on, the same call must reach vanilla code and
     * fail on the null arguments. Without this, a null return could also mean the call never
     * reached the ring search for some unrelated reason.
     */
    @Test
    void ringSearchStillRunsVanillaCodeWhileGenerationIsEnabled() {
        setEnabled(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> invokeRingSearch(0));

        assertInstanceOf(NullPointerException.class, thrown.getCause());
    }

    private static void setEnabled(boolean enabled) {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(enabled, CastleShiftConfig.Preset.DEFAULT, null, null)));
    }

    private static Object invokeRingSearch(int ring) throws ReflectiveOperationException {
        // A non-empty set, so vanilla's per-candidate loop reaches the null structure manager.
        Set<Holder<Structure>> structures = Set.of(Holder.direct(null));
        return getNearestGeneratedStructure.invoke(
                null,
                structures,
                null,
                null,
                0,
                0,
                ring,
                false,
                12345L,
                ConfigurableSpreadStructurePlacement.of(194572831));
    }
}
