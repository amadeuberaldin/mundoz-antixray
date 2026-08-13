package com.amadeu.mundozantixray;

import com.amadeu.mundozantixray.application.observation.ObservationPathEvaluationService;
import com.amadeu.mundozantixray.application.protection.ProtectionEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementIntegrationService;
import com.amadeu.mundozantixray.application.runtime.RuntimeDecisionComparison;
import com.amadeu.mundozantixray.application.runtime.ShadowEvaluationService;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import com.amadeu.mundozantixray.domain.policy.DefaultObservationPolicy;
import com.amadeu.mundozantixray.domain.protection.DefaultBlockProtectionRule;
import com.amadeu.mundozantixray.domain.protection.DefaultProtectionPolicy;
import com.amadeu.mundozantixray.domain.replacement.DefaultReplacementPolicy;
import com.amadeu.mundozantixray.domain.service.ObservationEvaluationService;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.MinecraftObservationPathClassifier;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.MinecraftObservationPathCollector;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.MinecraftObservationTargetSampler;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.MinecraftRuntimeShadowEvaluator;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.MinecraftSectionReplacementCandidateSource;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.ReplacementContextMapper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.List;
import java.util.function.Supplier;

final class AntiXrayShadowRuntime {
    static final String ENABLED_PROPERTY = "mundoz.antixray.v2-shadow";
    private static final boolean ENABLED = resolveEnabled(
            () -> System.getProperty(ENABLED_PROPERTY));

    private AntiXrayShadowRuntime() {}

    static boolean isEnabled() {
        return ENABLED;
    }

    static boolean parseEnabled(String propertyValue) {
        return Boolean.parseBoolean(propertyValue);
    }

    static boolean resolveEnabled(Supplier<String> propertyValue) {
        try {
            return parseEnabled(propertyValue.get());
        } catch (Throwable shadowFailure) {
            return false;
        }
    }

    static SectionEvaluation beginSection(LevelChunkSection section) {
        if (!ShadowRuntimeDiagnostics.isEnabled()) {
            return createSectionEvaluation(section);
        }

        long startedNanos = System.nanoTime();
        try {
            return createSectionEvaluation(section);
        } finally {
            recordSectionInitialization(System.nanoTime() - startedNanos);
        }
    }

    private static SectionEvaluation createSectionEvaluation(LevelChunkSection section) {
        try {
            List<ReplacementRepresentation> representations =
                    new ReplacementContextMapper().mapAvailableRepresentations(
                            new MinecraftSectionReplacementCandidateSource().candidatesFrom(section));
            return new SectionEvaluation(evaluator(), representations);
        } catch (Throwable shadowFailure) {
            recordFailure();
            return SectionEvaluation.disabled();
        }
    }

    static final class SectionEvaluation {
        private final MinecraftRuntimeShadowEvaluator evaluator;
        private final List<ReplacementRepresentation> representations;
        private boolean candidateEvaluated;

        SectionEvaluation(
                MinecraftRuntimeShadowEvaluator evaluator,
                List<ReplacementRepresentation> representations
        ) {
            this.evaluator = evaluator;
            this.representations = representations;
        }

        static SectionEvaluation disabled() {
            return new SectionEvaluation(null, List.of());
        }

        RuntimeDecisionComparison compare(
                ServerLevel level,
                ServerPlayer player,
                int targetX,
                int targetY,
                int targetZ,
                BlockState state,
                boolean v1Hides
        ) {
            if (evaluator == null || candidateEvaluated) {
                return RuntimeDecisionComparison.V2_CANNOT_EVALUATE;
            }

            boolean diagnosticsEnabled = ShadowRuntimeDiagnostics.isEnabled();
            try {
                MinecraftRuntimeShadowEvaluator.SupportStatus support = diagnosticsEnabled
                        ? evaluator.supportStatus(state)
                        : evaluator.supports(state)
                        ? MinecraftRuntimeShadowEvaluator.SupportStatus.SUPPORTED
                        : MinecraftRuntimeShadowEvaluator.SupportStatus.UNSUPPORTED;
                if (support != MinecraftRuntimeShadowEvaluator.SupportStatus.SUPPORTED) {
                    if (diagnosticsEnabled) {
                        if (support == MinecraftRuntimeShadowEvaluator.SupportStatus.FAILURE) {
                            recordFailure();
                        } else {
                            recordUnsupported();
                        }
                    }
                    return RuntimeDecisionComparison.V2_CANNOT_EVALUATE;
                }

                candidateEvaluated = true;
                if (!diagnosticsEnabled) {
                    return evaluator.compare(level, player,
                            new BlockPos(targetX, targetY, targetZ), state,
                            representations, v1Hides);
                }

                long startedNanos = System.nanoTime();
                MinecraftRuntimeShadowEvaluator.DiagnosticOutcome outcome = evaluator.evaluate(
                        level, player, new BlockPos(targetX, targetY, targetZ),
                        state, representations, v1Hides);
                recordOutcome(outcome, System.nanoTime() - startedNanos);
                return outcome.comparison();
            } catch (Throwable shadowFailure) {
                if (diagnosticsEnabled) {
                    recordFailure();
                }
                return RuntimeDecisionComparison.V2_CANNOT_EVALUATE;
            }
        }
    }

    private static MinecraftObservationPathCollector observationCollector() {
        return new MinecraftObservationPathCollector(
                new MinecraftObservationTargetSampler(),
                new MinecraftObservationPathClassifier());
    }

    private static void recordOutcome(
            MinecraftRuntimeShadowEvaluator.DiagnosticOutcome outcome,
            long elapsedNanos
    ) {
        try {
            ShadowRuntimeDiagnostics.Aggregate aggregate = ShadowRuntimeDiagnostics.aggregate();
            switch (outcome.kind()) {
                case COMPARABLE -> aggregate.recordComparison(outcome.comparison(), elapsedNanos);
                case UNSUPPORTED -> aggregate.recordUnsupported();
                case UNAVAILABLE -> aggregate.recordUnavailable(outcome.comparison(), elapsedNanos);
                case MISSING_REPLACEMENT ->
                        aggregate.recordMissingReplacement(outcome.comparison(), elapsedNanos);
                case FAILURE -> aggregate.recordFailure(elapsedNanos);
            }
        } catch (Throwable diagnosticFailure) {
            // Validation diagnostics are never authoritative.
        }
    }

    private static void recordSectionInitialization(long elapsedNanos) {
        try {
            ShadowRuntimeDiagnostics.aggregate().recordSectionInitialization(elapsedNanos);
        } catch (Throwable diagnosticFailure) {
            // Validation diagnostics are never authoritative.
        }
    }

    private static void recordUnsupported() {
        try {
            ShadowRuntimeDiagnostics.aggregate().recordUnsupported();
        } catch (Throwable diagnosticFailure) {
            // Validation diagnostics are never authoritative.
        }
    }

    private static void recordFailure() {
        if (!ShadowRuntimeDiagnostics.isEnabled()) {
            return;
        }
        try {
            ShadowRuntimeDiagnostics.aggregate().recordFailure();
        } catch (Throwable diagnosticFailure) {
            // Validation diagnostics are never authoritative.
        }
    }

    private static MinecraftRuntimeShadowEvaluator evaluator() {
        return new MinecraftRuntimeShadowEvaluator(
                observationCollector()::collect,
                new ShadowEvaluationService(
                        new ObservationPathEvaluationService(
                                new ObservationEvaluationService(new DefaultObservationPolicy())),
                        new ReplacementIntegrationService(
                                new ProtectionEvaluationService(
                                        new DefaultProtectionPolicy(new DefaultBlockProtectionRule())),
                                new ReplacementEvaluationService(new DefaultReplacementPolicy()))));
    }
}
