package com.castleshift.world.placement;

import com.castleshift.config.CastleShiftConfig;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

/**
 * Behaves like {@code minecraft:random_spread}, except that spacing/separation and the on/off
 * switch come from the mod config at chunk-generation time rather than from the structure set JSON.
 * Values are deliberately read live on every call so config changes apply without a restart.
 */
public class ConfigurableSpreadStructurePlacement extends StructurePlacement {

    public static final MapCodec<ConfigurableSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
            instance -> placementCodec(instance).apply(instance, ConfigurableSpreadStructurePlacement::new));

    public ConfigurableSpreadStructurePlacement(
            Vec3i locateOffset,
            FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<ExclusionZone> exclusionZone) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
    }

    public static ConfigurableSpreadStructurePlacement of(int salt) {
        return new ConfigurableSpreadStructurePlacement(
                Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, salt, Optional.empty());
    }

    public int spacing() {
        return CastleShiftConfig.get().generation().effectiveSpacing();
    }

    public int separation() {
        return CastleShiftConfig.get().generation().effectiveSeparation();
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        return isPotentialSpreadChunk(state.getLevelSeed(), chunkX, chunkZ);
    }

    boolean isPotentialSpreadChunk(long seed, int chunkX, int chunkZ) {
        if (!CastleShiftConfig.get().generation().enabled()) {
            return false;
        }
        ChunkPos potential = getPotentialStructureChunk(seed, chunkX, chunkZ);
        return potential.x == chunkX && potential.z == chunkZ;
    }

    /** Mirrors {@code RandomSpreadStructurePlacement#getPotentialStructureChunk} (LINEAR spread). */
    public ChunkPos getPotentialStructureChunk(long seed, int chunkX, int chunkZ) {
        int spacing = spacing();
        // The base class would divide by zero / draw from an empty range on degenerate values;
        // config input is clamped elsewhere, but worldgen must never crash on a hand-edited file.
        if (spacing < 1) {
            spacing = 1;
        }
        int offsetRange = Math.max(1, spacing - separation());
        int regionX = Math.floorDiv(chunkX, spacing);
        int regionZ = Math.floorDiv(chunkZ, spacing);
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, regionX, regionZ, salt());
        int offsetX = RandomSpreadType.LINEAR.evaluate(random, offsetRange);
        int offsetZ = RandomSpreadType.LINEAR.evaluate(random, offsetRange);
        return new ChunkPos(regionX * spacing + offsetX, regionZ * spacing + offsetZ);
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModPlacements.CONFIGURABLE_SPREAD;
    }
}
