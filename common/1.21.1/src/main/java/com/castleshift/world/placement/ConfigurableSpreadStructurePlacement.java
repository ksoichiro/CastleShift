package com.castleshift.world.placement;

import com.castleshift.config.CastleShiftConfig;
import com.castleshift.config.ConfigDefaults;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

/**
 * Behaves like {@code minecraft:random_spread}, except that spacing/separation and the on/off
 * switch come from the mod config at chunk-generation time rather than from the structure set JSON.
 * Values are deliberately read live on every call so config changes apply without a restart.
 *
 * <p>This extends {@link RandomSpreadStructurePlacement} rather than {@code StructurePlacement}
 * on purpose: {@code ChunkGenerator#findNearestMapStructure} (the code behind {@code /locate
 * structure} and explorer maps) collects candidates with {@code instanceof
 * RandomSpreadStructurePlacement}, so a direct {@code StructurePlacement} subclass is invisible to
 * it even though world generation works. Everything that path then does with the placement is a
 * virtual call ({@code spacing()} and {@code getPotentialStructureChunk(...)}), so the overrides
 * below keep the values config-driven. The spacing/separation handed to the super constructor are
 * never read: every vanilla method that reads those private fields is overridden here.
 */
public class ConfigurableSpreadStructurePlacement extends RandomSpreadStructurePlacement {

    public static final MapCodec<ConfigurableSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
            instance -> placementCodec(instance).apply(instance, ConfigurableSpreadStructurePlacement::new));

    public ConfigurableSpreadStructurePlacement(
            Vec3i locateOffset,
            FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<ExclusionZone> exclusionZone) {
        super(
                locateOffset,
                frequencyReductionMethod,
                frequency,
                salt,
                exclusionZone,
                ConfigDefaults.DEFAULT_SPACING,
                ConfigDefaults.DEFAULT_SEPARATION,
                RandomSpreadType.LINEAR);
    }

    public static ConfigurableSpreadStructurePlacement of(int salt) {
        return new ConfigurableSpreadStructurePlacement(
                Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, salt, Optional.empty());
    }

    @Override
    public int spacing() {
        return CastleShiftConfig.get().generation().effectiveSpacing();
    }

    @Override
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

    /**
     * Same algorithm as {@code RandomSpreadStructurePlacement#getPotentialStructureChunk} (LINEAR
     * spread), but reading spacing/separation from the config instead of the super class's private
     * final fields, which are fixed when the structure set JSON is parsed.
     */
    @Override
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
