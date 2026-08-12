package com.amadeu.mundozantixray;

import com.amadeu.mundozantixray.application.observation.ObservationPathEvaluationService;
import com.amadeu.mundozantixray.application.protection.ProtectionEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementIntegrationService;
import com.amadeu.mundozantixray.application.runtime.RuntimeDecisionComparison;
import com.amadeu.mundozantixray.application.runtime.ShadowEvaluationService;
import com.amadeu.mundozantixray.domain.model.BlockPosition;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import com.amadeu.mundozantixray.domain.policy.DefaultObservationPolicy;
import com.amadeu.mundozantixray.domain.protection.DefaultBlockProtectionRule;
import com.amadeu.mundozantixray.domain.protection.DefaultProtectionPolicy;
import com.amadeu.mundozantixray.domain.replacement.DefaultReplacementPolicy;
import com.amadeu.mundozantixray.domain.service.ObservationEvaluationService;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.MinecraftRuntimeShadowEvaluator;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AntiXrayShadowRuntimeTest {
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void clearProperty() {
        System.clearProperty(AntiXrayShadowRuntime.ENABLED_PROPERTY);
    }

    @Test
    void absentStartupPropertyResolvesToDisabled() {
        assertEquals(false, AntiXrayShadowRuntime.parseEnabled(null));
    }

    @Test
    void explicitFalseStartupPropertyResolvesToDisabled() {
        assertEquals(false, AntiXrayShadowRuntime.parseEnabled("false"));
    }

    @Test
    void explicitTrueStartupPropertyResolvesToEnabled() {
        assertEquals(true, AntiXrayShadowRuntime.parseEnabled("true"));
    }

    @Test
    void startupPropertyFailureResolvesToDisabled() {
        assertEquals(
                false,
                AntiXrayShadowRuntime.resolveEnabled(() -> {
                    throw new SecurityException("property access denied");
                })
        );
    }

    @Test
    void productionActivationIsImmutableAfterInitialization() {
        boolean startupDecision = AntiXrayShadowRuntime.isEnabled();
        System.setProperty(
                AntiXrayShadowRuntime.ENABLED_PROPERTY,
                Boolean.toString(!startupDecision)
        );

        assertEquals(startupDecision, AntiXrayShadowRuntime.isEnabled());
    }

    @Test
    void unsupportedCandidatesDoNotConsumeSingleSupportedCandidateBudget() {
        AtomicInteger observationCollections = new AtomicInteger();
        AntiXrayShadowRuntime.SectionEvaluation evaluation = evaluation(
                observationCollections,
                () -> contexts(ObservationPathBehavior.OCCLUDING),
                List.of(ReplacementRepresentation.STONE)
        );

        assertEquals(
                RuntimeDecisionComparison.V2_CANNOT_EVALUATE,
                compare(evaluation, Blocks.AMETHYST_BLOCK.defaultBlockState())
        );
        assertEquals(0, observationCollections.get());

        assertEquals(
                RuntimeDecisionComparison.BOTH_HIDE,
                compare(evaluation, Blocks.DIAMOND_ORE.defaultBlockState())
        );
        assertEquals(1, observationCollections.get());

        assertEquals(
                RuntimeDecisionComparison.V2_CANNOT_EVALUATE,
                compare(evaluation, Blocks.GOLD_ORE.defaultBlockState())
        );
        assertEquals(1, observationCollections.get());
    }

    @Test
    void shadowFailureIsContainedAndConsumesSupportedCandidateBudget() {
        AtomicInteger observationCollections = new AtomicInteger();
        AntiXrayShadowRuntime.SectionEvaluation evaluation = evaluation(
                observationCollections,
                () -> {
                    throw new IllegalStateException("shadow failure");
                },
                List.of(ReplacementRepresentation.STONE)
        );

        assertDoesNotThrow(
                () -> compare(evaluation, Blocks.DIAMOND_ORE.defaultBlockState())
        );
        assertEquals(1, observationCollections.get());

        assertDoesNotThrow(
                () -> compare(evaluation, Blocks.GOLD_ORE.defaultBlockState())
        );
        assertEquals(1, observationCollections.get());
    }

    @Test
    void unknownAndMissingReplacementRemainNonAuthoritative() {
        AtomicInteger unknownCollections = new AtomicInteger();
        AntiXrayShadowRuntime.SectionEvaluation unknownEvaluation = evaluation(
                unknownCollections,
                () -> contexts(ObservationPathBehavior.UNKNOWN),
                List.of(ReplacementRepresentation.STONE)
        );
        assertEquals(
                RuntimeDecisionComparison.V1_HIDES_V2_REVEALS,
                compare(
                        unknownEvaluation,
                        Blocks.DIAMOND_ORE.defaultBlockState()
                )
        );

        AtomicInteger missingReplacementCollections = new AtomicInteger();
        AntiXrayShadowRuntime.SectionEvaluation missingReplacementEvaluation =
                evaluation(
                        missingReplacementCollections,
                        () -> contexts(ObservationPathBehavior.OCCLUDING),
                        List.of()
                );
        assertEquals(
                RuntimeDecisionComparison.V1_HIDES_V2_REVEALS,
                compare(
                        missingReplacementEvaluation,
                        Blocks.DIAMOND_ORE.defaultBlockState()
                )
        );
    }

    private static AntiXrayShadowRuntime.SectionEvaluation evaluation(
            AtomicInteger observationCollections,
            ContextSupplier contexts,
            List<ReplacementRepresentation> representations
    ) {
        MinecraftRuntimeShadowEvaluator evaluator =
                new MinecraftRuntimeShadowEvaluator(
                        (level, player, target) -> {
                            observationCollections.incrementAndGet();
                            return contexts.get();
                        },
                        shadowService()
                );
        return new AntiXrayShadowRuntime.SectionEvaluation(
                evaluator,
                representations
        );
    }

    private static RuntimeDecisionComparison compare(
            AntiXrayShadowRuntime.SectionEvaluation evaluation,
            net.minecraft.world.level.block.state.BlockState state
    ) {
        return evaluation.compare(null, null, 0, 0, 0, state, true);
    }

    private static ShadowEvaluationService shadowService() {
        return new ShadowEvaluationService(
                new ObservationPathEvaluationService(
                        new ObservationEvaluationService(
                                new DefaultObservationPolicy()
                        )
                ),
                new ReplacementIntegrationService(
                        new ProtectionEvaluationService(
                                new DefaultProtectionPolicy(
                                        new DefaultBlockProtectionRule()
                                )
                        ),
                        new ReplacementEvaluationService(
                                new DefaultReplacementPolicy()
                        )
                )
        );
    }

    private static List<ObservationContext> contexts(
            ObservationPathBehavior behavior
    ) {
        return List.of(new ObservationContext(
                new BlockPosition(0, 0, 0),
                new BlockPosition(2, 0, 0),
                List.of(behavior)
        ));
    }

    @FunctionalInterface
    private interface ContextSupplier {
        List<ObservationContext> get();
    }
}
