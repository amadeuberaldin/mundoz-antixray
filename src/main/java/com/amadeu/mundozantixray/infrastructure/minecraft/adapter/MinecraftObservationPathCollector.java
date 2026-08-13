package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.BlockPosition;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MinecraftObservationPathCollector {
    private final MinecraftObservationTargetSampler targetSampler;
    private final MinecraftObservationPathClassifier classifier;

    public MinecraftObservationPathCollector(
            MinecraftObservationTargetSampler targetSampler,
            MinecraftObservationPathClassifier classifier
    ) {
        this.targetSampler = Objects.requireNonNull(targetSampler, "targetSampler");
        this.classifier = Objects.requireNonNull(classifier, "classifier");
    }

    public List<ObservationContext> collect(
            ServerLevel level,
            ServerPlayer player,
            BlockPos target
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(target, "target");

        Vec3 observationOrigin = player.getCamera().getEyePosition();
        return collect(
                observationOrigin,
                target,
                position -> stateIfLoaded(level, position)
        );
    }

    List<ObservationContext> collect(
            Vec3 observationOrigin,
            BlockPos target,
            BlockStateLookup blockStateLookup
    ) {
        Objects.requireNonNull(observationOrigin, "observationOrigin");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(blockStateLookup, "blockStateLookup");

        BlockPosition observerPosition = BlockPositionMapper.toDomain(
                BlockPos.containing(observationOrigin)
        );
        BlockPosition targetPosition = BlockPositionMapper.toDomain(target);
        List<ObservationContext> contexts = new ArrayList<>();

        for (Vec3 sample : targetSampler.samples(observationOrigin, target)) {
            List<ObservationPathBehavior> path = collectSample(
                    observationOrigin,
                    sample,
                    target,
                    blockStateLookup
            );
            contexts.add(new ObservationContext(
                    observerPosition,
                    targetPosition,
                    path
            ));
        }

        return List.copyOf(contexts);
    }

    private List<ObservationPathBehavior> collectSample(
            Vec3 observationOrigin,
            Vec3 targetSample,
            BlockPos target,
            BlockStateLookup blockStateLookup
    ) {
        List<ObservationPathBehavior> path = new ArrayList<>();

        BlockGetter.traverseBlocks(
                observationOrigin,
                targetSample,
                path,
                (behaviors, position) -> {
                    if (position.equals(target)) {
                        return null;
                    }

                    Optional<BlockState> state = blockStateLookup.stateAt(
                            position.immutable()
                    );
                    if (state.isEmpty()) {
                        behaviors.add(ObservationPathBehavior.UNKNOWN);
                        return Boolean.TRUE;
                    }

                    behaviors.add(classifier.classify(state.orElseThrow()));
                    return null;
                },
                behaviors -> Boolean.FALSE
        );

        return List.copyOf(path);
    }

    private static Optional<BlockState> stateIfLoaded(
            ServerLevel level,
            BlockPos position
    ) {
        if (level.isOutsideBuildHeight(position)) {
            return Optional.empty();
        }

        LevelChunk chunk = level.getChunkSource().getChunkNow(
                position.getX() >> 4,
                position.getZ() >> 4
        );
        if (chunk == null) {
            return Optional.empty();
        }

        return Optional.of(chunk.getBlockState(position));
    }

    @FunctionalInterface
    interface BlockStateLookup {
        Optional<BlockState> stateAt(BlockPos position);
    }
}
