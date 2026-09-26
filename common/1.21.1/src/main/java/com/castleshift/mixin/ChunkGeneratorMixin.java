package com.castleshift.mixin;

import com.castleshift.world.placement.ConfigurableSpreadStructurePlacement;
import com.mojang.datafixers.util.Pair;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Skips the {@code /locate} (and explorer map) ring search for castles while generation is
 * disabled. Vanilla probes one candidate per region over a 201x201 region grid and, for each one,
 * synchronously asks chunk storage whether that chunk already holds a start. That storage probe runs
 * before any placement check, so with nothing ever found it touched 40,401 region files and froze
 * the server thread for over two minutes even after the placement itself rejected every chunk.
 */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
    @Inject(
            method = "getNearestGeneratedStructure(Ljava/util/Set;Lnet/minecraft/world/level/LevelReader;"
                    + "Lnet/minecraft/world/level/StructureManager;IIIZJ"
                    + "Lnet/minecraft/world/level/levelgen/structure/placement/RandomSpreadStructurePlacement;)"
                    + "Lcom/mojang/datafixers/util/Pair;",
            at = @At("HEAD"),
            cancellable = true)
    private static void castleshift$skipRingSearchWhenDisabled(
            Set<Holder<Structure>> structures,
            LevelReader level,
            StructureManager structureManager,
            int sectionX,
            int sectionZ,
            int ring,
            boolean skipKnownStructures,
            long seed,
            RandomSpreadStructurePlacement placement,
            CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir) {
        if (placement instanceof ConfigurableSpreadStructurePlacement configurable
                && !configurable.isGenerationEnabled()) {
            cir.setReturnValue(null);
        }
    }
}
