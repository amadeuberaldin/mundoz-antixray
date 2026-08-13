package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

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
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinecraftRuntimeShadowEvaluatorTest {
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private final MinecraftRuntimeShadowEvaluator evaluator = evaluator();

    @Test
    void comparesRealMinecraftInputWithV2Decision() {
        assertEquals(
                RuntimeDecisionComparison.BOTH_HIDE,
                evaluator.compare(
                        Blocks.DIAMOND_ORE.defaultBlockState(),
                        List.of(ReplacementRepresentation.STONE),
                        true,
                        () -> contexts(ObservationPathBehavior.OCCLUDING)
                )
        );
        assertEquals(
                RuntimeDecisionComparison.V1_HIDES_V2_REVEALS,
                evaluator.compare(
                        Blocks.DIAMOND_ORE.defaultBlockState(),
                        List.of(ReplacementRepresentation.STONE),
                        true,
                        () -> contexts(ObservationPathBehavior.PASS_THROUGH)
                )
        );
    }

    @Test
    void unsupportedV1CandidateCannotBeEvaluatedByV2() {
        assertEquals(
                RuntimeDecisionComparison.V2_CANNOT_EVALUATE,
                evaluator.compare(
                        Blocks.AMETHYST_BLOCK.defaultBlockState(),
                        List.of(ReplacementRepresentation.STONE),
                        true,
                        () -> contexts(ObservationPathBehavior.OCCLUDING)
                )
        );
    }

    @Test
    void shadowFailureCannotEscapeIntoTheProductionPath() {
        assertEquals(
                RuntimeDecisionComparison.V2_CANNOT_EVALUATE,
                evaluator.compare(
                        Blocks.DIAMOND_ORE.defaultBlockState(),
                        List.of(ReplacementRepresentation.STONE),
                        true,
                        () -> {
                            throw new IllegalStateException("unavailable runtime input");
                        }
                )
        );
    }

    @Test
    void classifiesAlreadyProducedDiagnosticOutcomes() {
        assertEquals(MinecraftRuntimeShadowEvaluator.DiagnosticKind.UNAVAILABLE,
                evaluator.evaluate(Blocks.DIAMOND_ORE.defaultBlockState(),
                        List.of(ReplacementRepresentation.STONE), true,
                        () -> contexts(ObservationPathBehavior.UNKNOWN)).kind());
        assertEquals(MinecraftRuntimeShadowEvaluator.DiagnosticKind.MISSING_REPLACEMENT,
                evaluator.evaluate(Blocks.DIAMOND_ORE.defaultBlockState(),
                        List.of(), true,
                        () -> contexts(ObservationPathBehavior.OCCLUDING)).kind());
        assertEquals(MinecraftRuntimeShadowEvaluator.DiagnosticKind.UNSUPPORTED,
                evaluator.evaluate(Blocks.AMETHYST_BLOCK.defaultBlockState(),
                        List.of(ReplacementRepresentation.STONE), true,
                        () -> contexts(ObservationPathBehavior.OCCLUDING)).kind());
        assertEquals(MinecraftRuntimeShadowEvaluator.DiagnosticKind.FAILURE,
                evaluator.evaluate(Blocks.DIAMOND_ORE.defaultBlockState(),
                        List.of(ReplacementRepresentation.STONE), true,
                        () -> { throw new IllegalStateException("unavailable input"); }).kind());
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

    private static MinecraftRuntimeShadowEvaluator evaluator() {
        return new MinecraftRuntimeShadowEvaluator(
                new MinecraftObservationPathCollector(
                        new MinecraftObservationTargetSampler(),
                        new MinecraftObservationPathClassifier()
                )::collect,
                new ShadowEvaluationService(
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
                )
        );
    }
}
