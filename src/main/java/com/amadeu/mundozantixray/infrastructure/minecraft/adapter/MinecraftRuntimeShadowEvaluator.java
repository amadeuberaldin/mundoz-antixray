package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.application.runtime.RuntimeDecisionComparison;
import com.amadeu.mundozantixray.application.runtime.ShadowEvaluationService;
import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class MinecraftRuntimeShadowEvaluator {
    private final ObservationContextSource observationCollector;
    private final ShadowEvaluationService shadowService;

    public MinecraftRuntimeShadowEvaluator(
            ObservationContextSource observationCollector,
            ShadowEvaluationService shadowService
    ) {
        this.observationCollector = Objects.requireNonNull(observationCollector, "observationCollector");
        this.shadowService = Objects.requireNonNull(shadowService, "shadowService");
    }

    public boolean supports(BlockState state) {
        return supportStatus(state) == SupportStatus.SUPPORTED;
    }

    public SupportStatus supportStatus(BlockState state) {
        try {
            return BlockIdentityMapper.map(state).isPresent()
                    ? SupportStatus.SUPPORTED
                    : SupportStatus.UNSUPPORTED;
        } catch (Throwable shadowFailure) {
            return SupportStatus.FAILURE;
        }
    }

    public RuntimeDecisionComparison compare(
            ServerLevel level,
            ServerPlayer player,
            BlockPos target,
            BlockState state,
            List<ReplacementRepresentation> availableRepresentations,
            boolean v1Hides
    ) {
        return compare(state, availableRepresentations, v1Hides,
                () -> observationCollector.collect(level, player, target));
    }

    public DiagnosticOutcome evaluate(
            ServerLevel level,
            ServerPlayer player,
            BlockPos target,
            BlockState state,
            List<ReplacementRepresentation> availableRepresentations,
            boolean v1Hides
    ) {
        return evaluate(state, availableRepresentations, v1Hides,
                () -> observationCollector.collect(level, player, target));
    }

    RuntimeDecisionComparison compare(
            BlockState state,
            List<ReplacementRepresentation> availableRepresentations,
            boolean v1Hides,
            Supplier<List<ObservationContext>> observationContexts
    ) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(availableRepresentations, "availableRepresentations");
        Objects.requireNonNull(observationContexts, "observationContexts");

        Optional<BlockIdentity> block = BlockIdentityMapper.map(state);
        if (block.isEmpty()) {
            return RuntimeDecisionComparison.V2_CANNOT_EVALUATE;
        }

        try {
            boolean v2Hides = shadowService.shouldHide(
                    block.orElseThrow(), observationContexts.get(), availableRepresentations);
            return RuntimeDecisionComparison.compare(v1Hides, Optional.of(v2Hides));
        } catch (Throwable shadowFailure) {
            return RuntimeDecisionComparison.V2_CANNOT_EVALUATE;
        }
    }

    public DiagnosticOutcome evaluate(
            BlockState state,
            List<ReplacementRepresentation> availableRepresentations,
            boolean v1Hides,
            Supplier<List<ObservationContext>> observationContexts
    ) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(availableRepresentations, "availableRepresentations");
        Objects.requireNonNull(observationContexts, "observationContexts");

        Optional<BlockIdentity> block = BlockIdentityMapper.map(state);
        if (block.isEmpty()) {
            return DiagnosticOutcome.unsupported();
        }

        try {
            List<ObservationContext> contexts = observationContexts.get();
            boolean unavailable = contexts.stream()
                    .flatMap(context -> context.pathBeforeTarget().stream())
                    .anyMatch(behavior -> behavior == ObservationPathBehavior.UNKNOWN);
            ShadowEvaluationService.Evaluation evaluation = shadowService.evaluate(
                    block.orElseThrow(), contexts, availableRepresentations);
            RuntimeDecisionComparison comparison = RuntimeDecisionComparison.compare(
                    v1Hides, Optional.of(evaluation.shouldHide()));
            if (unavailable) {
                return DiagnosticOutcome.unavailable(comparison);
            }
            if (evaluation.missingReplacement()) {
                return DiagnosticOutcome.missingReplacement(comparison);
            }
            return DiagnosticOutcome.comparable(comparison);
        } catch (Throwable shadowFailure) {
            return DiagnosticOutcome.failure();
        }
    }

    public record DiagnosticOutcome(
            DiagnosticKind kind,
            RuntimeDecisionComparison comparison
    ) {
        static DiagnosticOutcome comparable(RuntimeDecisionComparison comparison) {
            return new DiagnosticOutcome(DiagnosticKind.COMPARABLE, comparison);
        }

        static DiagnosticOutcome unsupported() {
            return cannotEvaluate(DiagnosticKind.UNSUPPORTED);
        }

        static DiagnosticOutcome unavailable(RuntimeDecisionComparison comparison) {
            return new DiagnosticOutcome(DiagnosticKind.UNAVAILABLE, comparison);
        }

        static DiagnosticOutcome missingReplacement(RuntimeDecisionComparison comparison) {
            return new DiagnosticOutcome(DiagnosticKind.MISSING_REPLACEMENT, comparison);
        }

        static DiagnosticOutcome failure() {
            return cannotEvaluate(DiagnosticKind.FAILURE);
        }

        private static DiagnosticOutcome cannotEvaluate(DiagnosticKind kind) {
            return new DiagnosticOutcome(kind, RuntimeDecisionComparison.V2_CANNOT_EVALUATE);
        }
    }

    public enum DiagnosticKind {
        COMPARABLE, UNSUPPORTED, UNAVAILABLE, MISSING_REPLACEMENT, FAILURE
    }

    public enum SupportStatus {
        SUPPORTED, UNSUPPORTED, FAILURE
    }

    @FunctionalInterface
    public interface ObservationContextSource {
        List<ObservationContext> collect(ServerLevel level, ServerPlayer player, BlockPos target);
    }
}
