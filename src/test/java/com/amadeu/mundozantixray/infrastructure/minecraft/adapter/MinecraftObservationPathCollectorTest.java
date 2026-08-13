package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinecraftObservationPathCollectorTest {
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static final Vec3 ORIGIN = new Vec3(0.5D, 0.5D, 0.5D);
    private static final BlockPos TARGET = new BlockPos(3, 0, 0);

    private final MinecraftObservationPathCollector collector =
            new MinecraftObservationPathCollector(
                    new MinecraftObservationTargetSampler(),
                    new MinecraftObservationPathClassifier()
            );

    @Test
    void collectsCrossedCellsInOriginToTargetOrderAndExcludesTarget() {
        List<BlockPos> reads = new ArrayList<>();

        List<ObservationContext> contexts = collector.collect(
                ORIGIN,
                TARGET,
                position -> {
                    reads.add(position);
                    return Optional.of(Blocks.AIR.defaultBlockState());
                }
        );

        assertEquals(
                List.of(
                        new BlockPos(0, 0, 0),
                        new BlockPos(1, 0, 0),
                        new BlockPos(2, 0, 0)
                ),
                reads.subList(0, 3)
        );
        assertFalse(reads.contains(TARGET));
        for (ObservationContext context : contexts) {
            assertEquals(
                    List.of(
                            ObservationPathBehavior.PASS_THROUGH,
                            ObservationPathBehavior.PASS_THROUGH,
                            ObservationPathBehavior.PASS_THROUGH
                    ),
                    context.pathBeforeTarget()
            );
        }
    }

    @Test
    void originCellIsIncludedUnlessItIsTheTarget() {
        assertEquals(
                List.of(ObservationPathBehavior.OCCLUDING),
                collectSinglePath(
                        ORIGIN,
                        new BlockPos(1, 0, 0),
                        position -> Optional.of(Blocks.STONE.defaultBlockState())
                )
        );
        assertEquals(
                List.of(),
                collectSinglePath(
                        ORIGIN,
                        BlockPos.ZERO,
                        position -> Optional.of(Blocks.STONE.defaultBlockState())
                )
        );
    }

    @Test
    void delegatesEveryReadableStateToTheExistingClassifier() {
        assertEquals(
                List.of(
                        ObservationPathBehavior.PASS_THROUGH,
                        ObservationPathBehavior.PASS_THROUGH,
                        ObservationPathBehavior.OCCLUDING
                ),
                collectSinglePath(
                        ORIGIN,
                        TARGET,
                        position -> Optional.of(stateFor(position))
                )
        );
    }

    @Test
    void unavailablePositionAppendsUnknownAndStopsThatSample() {
        List<BlockPos> reads = new ArrayList<>();

        List<ObservationPathBehavior> path = collectSinglePath(
                ORIGIN,
                new BlockPos(5, 0, 0),
                position -> {
                    reads.add(position);
                    return position.getX() == 2
                            ? Optional.empty()
                            : Optional.of(Blocks.AIR.defaultBlockState());
                }
        );

        assertEquals(
                List.of(
                        ObservationPathBehavior.PASS_THROUGH,
                        ObservationPathBehavior.PASS_THROUGH,
                        ObservationPathBehavior.UNKNOWN
                ),
                path
        );
        assertEquals(new BlockPos(2, 0, 0), reads.getLast());
    }

    @Test
    void contextsAndPathsAreImmutable() {
        List<ObservationContext> contexts = collector.collect(
                ORIGIN,
                TARGET,
                position -> Optional.of(Blocks.AIR.defaultBlockState())
        );

        assertThrows(UnsupportedOperationException.class, () -> contexts.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> contexts.getFirst().pathBeforeTarget().clear());
    }

    @Test
    void nullInputsAndDependenciesAreRejected() {
        MinecraftObservationTargetSampler sampler = new MinecraftObservationTargetSampler();
        MinecraftObservationPathClassifier classifier = new MinecraftObservationPathClassifier();

        assertThrows(NullPointerException.class,
                () -> new MinecraftObservationPathCollector(null, classifier));
        assertThrows(NullPointerException.class,
                () -> new MinecraftObservationPathCollector(sampler, null));
        assertThrows(NullPointerException.class,
                () -> collector.collect(null, TARGET, position -> Optional.empty()));
        assertThrows(NullPointerException.class,
                () -> collector.collect(ORIGIN, null, position -> Optional.empty()));
        assertThrows(NullPointerException.class,
                () -> collector.collect(ORIGIN, TARGET, null));
    }

    private List<ObservationPathBehavior> collectSinglePath(
            Vec3 origin,
            BlockPos target,
            MinecraftObservationPathCollector.BlockStateLookup lookup
    ) {
        return collector.collect(origin, target, lookup)
                .getFirst()
                .pathBeforeTarget();
    }

    private static BlockState stateFor(BlockPos position) {
        return switch (position.getX()) {
            case 0 -> Blocks.WATER.defaultBlockState();
            case 1 -> Blocks.GLASS.defaultBlockState();
            default -> Blocks.LAVA.defaultBlockState();
        };
    }
}
