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
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

class ShadowValidationEnabledRuntimeTest {
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void startupActivationIsEnabledWithoutChangingShadowActivation() {
        assertTrue(ShadowRuntimeDiagnostics.isEnabled());
        assertTrue(AntiXrayShadowRuntime.isEnabled());
    }

    @Test
    void diagnosticsObserveOnlyTheExistingSingleSupportedEvaluation() {
        AtomicInteger collections = new AtomicInteger();
        MinecraftRuntimeShadowEvaluator evaluator = new MinecraftRuntimeShadowEvaluator(
                (level, player, target) -> {
                    collections.incrementAndGet();
                    return List.of(new ObservationContext(
                            new BlockPosition(0, 0, 0),
                            new BlockPosition(2, 0, 0),
                            List.of(ObservationPathBehavior.OCCLUDING)));
                },
                shadowService());
        AntiXrayShadowRuntime.SectionEvaluation section =
                new AntiXrayShadowRuntime.SectionEvaluation(
                        evaluator, List.of(ReplacementRepresentation.STONE));
        ShadowRuntimeDiagnostics.Summary before =
                ShadowRuntimeDiagnostics.aggregate().snapshot();

        section.compare(null, null, 0, 0, 0,
                Blocks.AMETHYST_BLOCK.defaultBlockState(), true);
        section.compare(null, null, 0, 0, 0,
                Blocks.DIAMOND_ORE.defaultBlockState(), true);
        section.compare(null, null, 1, 0, 0,
                Blocks.GOLD_ORE.defaultBlockState(), true);

        ShadowRuntimeDiagnostics.Summary after =
                ShadowRuntimeDiagnostics.aggregate().snapshot();
        assertEquals(1, collections.get());
        assertEquals(1L, after.unsupported() - before.unsupported());
        assertEquals(1L, after.evaluationCount() - before.evaluationCount());
        assertEquals(1L, after.agreements() - before.agreements());
    }

    @Test
    void diagnosticsCannotChangeSerializedOutputOrAuthoritativeSection() {
        var section = AntiXrayEnabledShadowIsolationTest.sectionWith(
                Blocks.STONE.defaultBlockState());
        var fixture = AntiXrayEnabledShadowIsolationTest.runtimeFor(section);

        byte[] diagnosticsDisabled;
        try (MockedStatic<ShadowRuntimeDiagnostics> diagnostics = mockStatic(
                ShadowRuntimeDiagnostics.class, CALLS_REAL_METHODS)) {
            diagnostics.when(ShadowRuntimeDiagnostics::isEnabled).thenReturn(false);
            diagnosticsDisabled = AntiXrayEnabledShadowIsolationTest.serialize(
                    section, fixture, true);
        }
        byte[] diagnosticsEnabled = AntiXrayEnabledShadowIsolationTest.serialize(
                section, fixture, true);

        assertArrayEquals(diagnosticsDisabled, diagnosticsEnabled);
        assertSame(Blocks.DIAMOND_ORE.defaultBlockState(),
                section.getBlockState(0, 0, 0));
    }

    @Test
    void diagnosticFailureCannotAffectExistingShadowEvaluation() {
        MinecraftRuntimeShadowEvaluator evaluator = new MinecraftRuntimeShadowEvaluator(
                (level, player, target) -> List.of(new ObservationContext(
                        new BlockPosition(0, 0, 0),
                        new BlockPosition(2, 0, 0),
                        List.of(ObservationPathBehavior.OCCLUDING))),
                shadowService());
        AntiXrayShadowRuntime.SectionEvaluation section =
                new AntiXrayShadowRuntime.SectionEvaluation(
                        evaluator, List.of(ReplacementRepresentation.STONE));

        try (MockedStatic<ShadowRuntimeDiagnostics> diagnostics = mockStatic(
                ShadowRuntimeDiagnostics.class, CALLS_REAL_METHODS)) {
            diagnostics.when(ShadowRuntimeDiagnostics::aggregate)
                    .thenThrow(new IllegalStateException("diagnostics unavailable"));
            assertDoesNotThrow(() -> assertEquals(
                    RuntimeDecisionComparison.BOTH_HIDE,
                    section.compare(null, null, 0, 0, 0,
                            Blocks.DIAMOND_ORE.defaultBlockState(), true)));
        }
    }

    private static ShadowEvaluationService shadowService() {
        return new ShadowEvaluationService(
                new ObservationPathEvaluationService(
                        new ObservationEvaluationService(new DefaultObservationPolicy())),
                new ReplacementIntegrationService(
                        new ProtectionEvaluationService(
                                new DefaultProtectionPolicy(new DefaultBlockProtectionRule())),
                        new ReplacementEvaluationService(new DefaultReplacementPolicy())));
    }
}
