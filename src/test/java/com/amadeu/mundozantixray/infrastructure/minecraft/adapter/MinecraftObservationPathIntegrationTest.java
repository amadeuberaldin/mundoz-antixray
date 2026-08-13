package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.application.observation.ObservationPathEvaluationService;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import com.amadeu.mundozantixray.domain.policy.DefaultObservationPolicy;
import com.amadeu.mundozantixray.domain.service.ObservationEvaluationService;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class MinecraftObservationPathIntegrationTest {
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
    private final ObservationPathEvaluationService evaluationService =
            new ObservationPathEvaluationService(
                    new ObservationEvaluationService(
                            new DefaultObservationPolicy()
                    )
            );

    @Test
    void airPathIsObserved() {
        assertDecision(ObservationDecision.OBSERVED, Blocks.AIR, Blocks.AIR, Blocks.AIR);
    }

    @Test
    void stoneBeforeTargetIsNotObserved() {
        assertDecision(ObservationDecision.NOT_OBSERVED, Blocks.AIR, Blocks.STONE, Blocks.AIR);
    }

    @Test
    void waterPathIsObserved() {
        assertDecision(ObservationDecision.OBSERVED, Blocks.WATER, Blocks.WATER, Blocks.WATER);
    }

    @Test
    void glassPathIsObserved() {
        assertDecision(ObservationDecision.OBSERVED, Blocks.GLASS, Blocks.GLASS, Blocks.GLASS);
    }

    @Test
    void approvedPartialObjectPathIsObserved() {
        assertDecision(ObservationDecision.OBSERVED,
                Blocks.AIR, Blocks.CRAFTING_TABLE, Blocks.AIR);
    }

    @Test
    void leverPathIsObserved() {
        assertDecision(ObservationDecision.OBSERVED,
                Blocks.AIR, Blocks.LEVER, Blocks.AIR);
    }

    @Test
    void brewingStandPathIsObserved() {
        assertDecision(ObservationDecision.OBSERVED,
                Blocks.AIR, Blocks.BREWING_STAND, Blocks.AIR);
    }

    @Test
    void lavaBeforeTargetIsNotObserved() {
        assertDecision(ObservationDecision.NOT_OBSERVED, Blocks.AIR, Blocks.LAVA, Blocks.AIR);
    }

    @Test
    void everyDryAndWaterloggedCopperGratePathIsObserved() {
        MinecraftObservationPathClassifier classifier =
                new MinecraftObservationPathClassifier();

        for (Block grate : Blocks.COPPER_GRATE.asList()) {
            for (boolean waterlogged : List.of(false, true)) {
                BlockState expectedGrateState = grate.defaultBlockState().setValue(
                        BlockStateProperties.WATERLOGGED, waterlogged
                );
                List<TraversedState> traversedStates = new ArrayList<>();

                List<ObservationContext> contexts = collector.collect(
                        ORIGIN, TARGET, position -> {
                            BlockState state = position.getX() == 1
                                    ? expectedGrateState
                                    : Blocks.AIR.defaultBlockState();
                            traversedStates.add(new TraversedState(position, state));
                            return Optional.of(state);
                        }
                );

                List<TraversedState> grateReads = traversedStates.stream()
                        .filter(read -> read.position().equals(new BlockPos(1, 0, 0)))
                        .toList();
                String description = grate + " waterlogged=" + waterlogged;

                assertFalse(grateReads.isEmpty(), description);
                for (TraversedState grateRead : grateReads) {
                    assertSame(expectedGrateState, grateRead.state(), description);
                    assertEquals(ObservationPathBehavior.PASS_THROUGH,
                            classifier.classify(grateRead.state()), description);
                }
                for (ObservationContext context : contexts) {
                    assertEquals(List.of(
                                    ObservationPathBehavior.PASS_THROUGH,
                                    ObservationPathBehavior.PASS_THROUGH,
                                    ObservationPathBehavior.PASS_THROUGH
                            ), context.pathBeforeTarget(), description);
                    assertFalse(context.pathBeforeTarget().contains(
                            ObservationPathBehavior.OCCLUDING), description);
                }
                assertEquals(ObservationDecision.OBSERVED,
                        evaluationService.evaluate(contexts), description);
            }
        }
    }

    @Test
    void unavailablePathFailsVisible() {
        assertEquals(
                ObservationDecision.OBSERVED,
                evaluationService.evaluate(
                        collector.collect(
                                ORIGIN,
                                TARGET,
                                position -> position.getX() == 1
                                        ? Optional.empty()
                                        : Optional.of(Blocks.STONE.defaultBlockState())
                        )
                )
        );
    }

    @Test
    void targetNeverOccludesItself() {
        assertEquals(
                ObservationDecision.OBSERVED,
                evaluationService.evaluate(
                        collector.collect(
                                ORIGIN,
                                TARGET,
                                position -> Optional.of(
                                        position.equals(TARGET)
                                                ? Blocks.STONE.defaultBlockState()
                                                : Blocks.AIR.defaultBlockState()
                                )
                        )
                )
        );
    }

    private void assertDecision(
            ObservationDecision expected,
            Block originBlock,
            Block middleBlock,
            Block beforeTargetBlock
    ) {
        List<Block> blocks = List.of(originBlock, middleBlock, beforeTargetBlock);

        assertEquals(
                expected,
                evaluationService.evaluate(
                        collector.collect(
                                ORIGIN,
                                TARGET,
                                position -> Optional.of(
                                        blocks.get(position.getX()).defaultBlockState()
                                )
                        )
                )
        );
    }

    private record TraversedState(BlockPos position, BlockState state) {}
}
