package com.castleshift.world.placement;

import com.castleshift.config.CastleShiftConfig;
import com.castleshift.config.ConfigDefaults;
import com.mojang.serialization.Codec;
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
 *
 * <p>MC 1.20.1 has no {@code StructurePlacement#applyAdditionalChunkRestrictions}: frequency
 * reduction and the exclusion zone are folded directly into {@code isStructureChunk}, which calls
 * {@link #isPlacementChunk} first and short-circuits immediately when it returns {@code false}. So
 * the {@link #isPlacementChunk} override alone is enough to gate ordinary worldgen here; there is no
 * separate method to override for it. The {@code /locate} ring search
 * ({@code ChunkGenerator#getNearestGeneratedStructure}) does not consult either method at all in
 * this version, which is exactly why {@code ChunkGeneratorMixin} exists: it short-circuits the whole
 * search at the top of that method regardless of what vanilla does inside it.
 *
 * <p>{@link #getPotentialStructureChunk} has no {@code enabled()} guard: it only answers "which
 * chunk of this region would hold a castle" and must return a position. The on/off switch lives in
 * {@link #isPlacementChunk}.
 */
public class ConfigurableSpreadStructurePlacement extends RandomSpreadStructurePlacement {

    public static final Codec<ConfigurableSpreadStructurePlacement> CODEC = RecordCodecBuilder.create(
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

    /**
     * Rejects every chunk while generation is disabled. In 1.20.1, {@code isStructureChunk} calls
     * this first and returns {@code false} immediately without checking frequency or the exclusion
     * zone, so this single override is enough to disable ordinary worldgen (unlike 1.21.x, which
     * needs a second override for the {@code applyAdditionalChunkRestrictions} path).
     */
    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        return isPotentialSpreadChunk(state.getLevelSeed(), chunkX, chunkZ);
    }

    /** Read live, like spacing/separation. Also consulted by {@code ChunkGeneratorMixin}. */
    public boolean isGenerationEnabled() {
        return CastleShiftConfig.get().generation().enabled();
    }

    boolean isPotentialSpreadChunk(long seed, int chunkX, int chunkZ) {
        if (!isGenerationEnabled()) {
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
