package com.castleshift.world.placement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.castleshift.config.CastleShiftConfig;
import com.castleshift.config.ConfigDefaults;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.Vec3i;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConfigurableSpreadStructurePlacementTest {

    private static final int SALT = 194572831;

    // StructurePlacement's static init touches BuiltInRegistries, which requires MC bootstrap.
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetConfig() {
        CastleShiftConfig.set(ConfigDefaults.defaults());
    }

    @Test
    void disabledConfigNeverProducesAPlacementChunk() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(false, CastleShiftConfig.Preset.DEFAULT, null, null)));

        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);

        for (int x = -64; x < 64; x++) {
            for (int z = -64; z < 64; z++) {
                assertFalse(
                        PlacementTestInvoker.isPotentialSpreadChunk(placement, 0L, x, z),
                        "expected no placement chunk when generation is disabled");
            }
        }
    }

    @Test
    void enabledConfigProducesPlacementChunks() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, null, null)));

        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);

        boolean any = false;
        for (int x = -64; x < 64 && !any; x++) {
            for (int z = -64; z < 64 && !any; z++) {
                any = PlacementTestInvoker.isPotentialSpreadChunk(placement, 0L, x, z);
            }
        }
        assertTrue(any, "expected at least one placement chunk when generation is enabled");
    }

    @Test
    void enabledConfigUsesEffectiveSpacingFromPreset() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.FREQUENT, null, null)));

        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);

        assertEquals(ConfigDefaults.FREQUENT_SPACING, placement.spacing());
        assertEquals(ConfigDefaults.FREQUENT_SEPARATION, placement.separation());
        assertNotEquals(ConfigDefaults.DEFAULT_SPACING, placement.spacing());
    }

    @Test
    void customSpacingOverridesPreset() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, 77, null)));

        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);

        assertEquals(77, placement.spacing());
    }

    /**
     * ChunkGenerator#findNearestMapStructure (the /locate structure and explorer map path) only
     * considers placements that pass `instanceof RandomSpreadStructurePlacement`. Without this,
     * castles generate but can never be located.
     */
    @Test
    void isRecognizedByVanillaAsARandomSpreadPlacement() {
        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);

        assertTrue(
                placement instanceof RandomSpreadStructurePlacement,
                "vanilla /locate only sees RandomSpreadStructurePlacement instances");
    }

    /**
     * The values vanilla's locate path reads are the overridden accessors, not the super class's
     * private final fields, so they must track the config even after construction.
     */
    @Test
    void vanillaAccessorsReturnLiveConfigValuesNotTheConstructorArguments() {
        // Typed as the vanilla class on purpose: this is how ChunkGenerator holds the reference.
        RandomSpreadStructurePlacement asVanilla = PlacementTestInvoker.create(SALT);

        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, null, null)));
        ChunkPos underDefault = asVanilla.getPotentialStructureChunk(0L, 1000, 1000);

        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.SPARSE, null, null)));

        assertEquals(ConfigDefaults.SPARSE_SPACING, asVanilla.spacing());
        assertEquals(ConfigDefaults.SPARSE_SEPARATION, asVanilla.separation());
        assertNotEquals(underDefault, asVanilla.getPotentialStructureChunk(0L, 1000, 1000));
    }

    @Test
    void codecParsesAStructureSetPlacementBlockWithSaltOnly() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "castleshift:configurable_spread");
        json.addProperty("salt", SALT);

        ConfigurableSpreadStructurePlacement parsed = ConfigurableSpreadStructurePlacement.CODEC
                .codec()
                .parse(JsonOps.INSTANCE, json)
                .getOrThrow();

        // The salt is stored on the vanilla base class (protected accessor), so assert it
        // indirectly: the same salt must yield the same spread as a directly constructed instance.
        assertEquals(
                PlacementTestInvoker.create(SALT).getPotentialStructureChunk(0L, 0, 0),
                parsed.getPotentialStructureChunk(0L, 0, 0));
        assertEquals(ModPlacements.CONFIGURABLE_SPREAD, parsed.type());
    }

    /**
     * The reimplemented spread must stay bit-for-bit identical to the {@code minecraft:random_spread}
     * placement this structure set used before, otherwise switching presets would silently shift
     * (or, worse, flatten) where castles land. Checked across many regions under FREQUENT, the
     * tightest preset, because that is where a degenerate per-region seed would show up first.
     */
    @Test
    void matchesVanillaRandomSpreadForTheSameSpacingAndSeparation() {
        for (CastleShiftConfig.Preset preset : CastleShiftConfig.Preset.values()) {
            CastleShiftConfig.set(
                    new CastleShiftConfig(new CastleShiftConfig.Generation(true, preset, null, null)));
            ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);
            RandomSpreadStructurePlacement vanilla = new RandomSpreadStructurePlacement(
                    Vec3i.ZERO,
                    StructurePlacement.FrequencyReductionMethod.DEFAULT,
                    1.0F,
                    SALT,
                    Optional.empty(),
                    placement.spacing(),
                    placement.separation(),
                    RandomSpreadType.LINEAR);

            for (long seed : new long[] {0L, 12345L, -98765432101L}) {
                for (int x = -2048; x <= 2048; x += 37) {
                    for (int z = -2048; z <= 2048; z += 53) {
                        assertEquals(
                                vanilla.getPotentialStructureChunk(seed, x, z),
                                placement.getPotentialStructureChunk(seed, x, z),
                                "preset " + preset + " diverged from vanilla random_spread at " + x + "," + z);
                    }
                }
            }
        }
    }

    /**
     * Guards the per-region seeding: every region must draw its own offset, so neighbouring castles
     * never sit at the same place inside their grid cell. A copy-paste slip that fed a constant
     * instead of regionX/regionZ into setLargeFeatureWithSalt would collapse this set to one entry.
     */
    @Test
    void offsetsWithinARegionVaryFromRegionToRegion() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.FREQUENT, null, null)));
        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);
        int spacing = placement.spacing();
        int offsetRange = spacing - placement.separation();

        Set<Long> distinctOffsets = new HashSet<>();
        for (int regionX = 0; regionX < 32; regionX++) {
            for (int regionZ = 0; regionZ < 32; regionZ++) {
                ChunkPos chunk = placement.getPotentialStructureChunk(12345L, regionX * spacing, regionZ * spacing);
                int offsetX = chunk.x - regionX * spacing;
                int offsetZ = chunk.z - regionZ * spacing;
                assertTrue(offsetX >= 0 && offsetX < offsetRange, "offsetX out of range: " + offsetX);
                assertTrue(offsetZ >= 0 && offsetZ < offsetRange, "offsetZ out of range: " + offsetZ);
                distinctOffsets.add(((long) offsetX << 32) | (offsetZ & 0xFFFFFFFFL));
            }
        }

        // 1024 regions over a 12x12 offset grid: anything close to 1 means the seeding is degenerate.
        assertTrue(distinctOffsets.size() > 100, "expected varied per-region offsets, got " + distinctOffsets.size());
    }

    @Test
    void spacingIsReadLiveSoConfigChangesTakeEffectWithoutRebuildingThePlacement() {
        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.DEFAULT, null, null)));

        ConfigurableSpreadStructurePlacement placement = PlacementTestInvoker.create(SALT);
        assertEquals(ConfigDefaults.DEFAULT_SPACING, placement.spacing());

        CastleShiftConfig.set(new CastleShiftConfig(
                new CastleShiftConfig.Generation(true, CastleShiftConfig.Preset.SPARSE, null, null)));

        assertEquals(ConfigDefaults.SPARSE_SPACING, placement.spacing());
    }
}
