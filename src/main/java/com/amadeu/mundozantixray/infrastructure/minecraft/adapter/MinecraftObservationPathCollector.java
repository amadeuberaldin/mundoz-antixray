package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.BlockPosition;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

    public TraceResult collectTrace(
            ServerLevel level,
            Entity camera,
            BlockPos target
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(camera, "camera");
        Objects.requireNonNull(target, "target");

        Vec3 cameraEye = camera.getEyePosition();
        return collectTrace(
                cameraEye,
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

    TraceResult collectTrace(
            Vec3 cameraEye,
            BlockPos target,
            BlockStateLookup blockStateLookup
    ) {
        Objects.requireNonNull(cameraEye, "cameraEye");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(blockStateLookup, "blockStateLookup");

        BlockPos observerBlockPos = BlockPos.containing(cameraEye);
        BlockPosition observerPosition = BlockPositionMapper.toDomain(observerBlockPos);
        BlockPosition targetPosition = BlockPositionMapper.toDomain(target);
        List<SampleTrace> samples = new ArrayList<>();
        List<ObservationContext> contexts = new ArrayList<>();
        List<Vec3> endpoints = targetSampler.samples(cameraEye, target);

        for (int index = 0; index < endpoints.size(); index++) {
            Vec3 endpoint = endpoints.get(index);
            List<VisitedBlock> visited = collectSampleTrace(
                    cameraEye,
                    endpoint,
                    target,
                    blockStateLookup
            );
            List<ObservationPathBehavior> path = visited.stream()
                    .map(VisitedBlock::behavior)
                    .toList();
            ObservationContext context = new ObservationContext(
                    observerPosition,
                    targetPosition,
                    path
            );
            samples.add(new SampleTrace(index, endpoint, visited));
            contexts.add(context);
        }

        return new TraceResult(
                cameraEye,
                observerBlockPos,
                samples,
                contexts
        );
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

    private List<VisitedBlock> collectSampleTrace(
            Vec3 observationOrigin,
            Vec3 targetSample,
            BlockPos target,
            BlockStateLookup blockStateLookup
    ) {
        List<VisitedBlock> visited = new ArrayList<>();

        BlockGetter.traverseBlocks(
                observationOrigin,
                targetSample,
                visited,
                (blocks, position) -> {
                    if (position.equals(target)) {
                        return null;
                    }

                    BlockPos immutablePosition = position.immutable();
                    Optional<BlockState> state = blockStateLookup.stateAt(
                            immutablePosition
                    );
                    if (state.isEmpty()) {
                        blocks.add(new VisitedBlock(
                                immutablePosition,
                                Optional.empty(),
                                ObservationPathBehavior.UNKNOWN
                        ));
                        return Boolean.TRUE;
                    }

                    BlockState readState = state.orElseThrow();
                    blocks.add(new VisitedBlock(
                            immutablePosition,
                            Optional.of(readState),
                            classifier.classify(readState)
                    ));
                    return null;
                },
                blocks -> Boolean.FALSE
        );

        return List.copyOf(visited);
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

    public record TraceResult(
            Vec3 cameraEye,
            BlockPos observerBlockPos,
            List<SampleTrace> samples,
            List<ObservationContext> contexts
    ) {
        public TraceResult {
            Objects.requireNonNull(cameraEye, "cameraEye");
            observerBlockPos = Objects.requireNonNull(
                    observerBlockPos,
                    "observerBlockPos"
            ).immutable();
            samples = List.copyOf(Objects.requireNonNull(samples, "samples"));
            contexts = List.copyOf(Objects.requireNonNull(contexts, "contexts"));
            if (samples.size() != contexts.size()) {
                throw new IllegalArgumentException(
                        "samples and contexts must have equal size"
                );
            }
        }
    }

    public record SampleTrace(
            int index,
            Vec3 endpoint,
            List<VisitedBlock> visited
    ) {
        public SampleTrace {
            Objects.requireNonNull(endpoint, "endpoint");
            visited = List.copyOf(Objects.requireNonNull(visited, "visited"));
        }
    }

    public record VisitedBlock(
            BlockPos position,
            Optional<BlockState> state,
            ObservationPathBehavior behavior
    ) {
        public VisitedBlock {
            position = Objects.requireNonNull(position, "position").immutable();
            state = Objects.requireNonNull(state, "state");
            Objects.requireNonNull(behavior, "behavior");
            if (state.isEmpty() != (behavior == ObservationPathBehavior.UNKNOWN)) {
                throw new IllegalArgumentException(
                        "unavailable state must correspond exactly to UNKNOWN"
                );
            }
        }
    }

    @FunctionalInterface
    interface BlockStateLookup {
        Optional<BlockState> stateAt(BlockPos position);
    }
}
