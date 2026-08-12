package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.application.runtime.RuntimeDecisionComparison;
import com.amadeu.mundozantixray.application.runtime.ShadowEvaluationService;
import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
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
        this.observationCollector = Objects.requireNonNull(
                observationCollector,
                "observationCollector"
        );
        this.shadowService = Objects.requireNonNull(
                shadowService,
                "shadowService"
        );
    }

    public boolean supports(BlockState state) {
        try {
            return BlockIdentityMapper.map(state).isPresent();
        } catch (Throwable shadowFailure) {
            return false;
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
        return compare(
                state,
                availableRepresentations,
                v1Hides,
                () -> observationCollector.collect(level, player, target)
        );
    }

    RuntimeDecisionComparison compare(
            BlockState state,
            List<ReplacementRepresentation> availableRepresentations,
            boolean v1Hides,
            Supplier<List<ObservationContext>> observationContexts
    ) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(
                availableRepresentations,
                "availableRepresentations"
        );
        Objects.requireNonNull(observationContexts, "observationContexts");

        Optional<BlockIdentity> block = BlockIdentityMapper.map(state);
        if (block.isEmpty()) {
            return RuntimeDecisionComparison.V2_CANNOT_EVALUATE;
        }

        try {
            boolean v2Hides = shadowService.shouldHide(
                    block.orElseThrow(),
                    observationContexts.get(),
                    availableRepresentations
            );
            return RuntimeDecisionComparison.compare(
                    v1Hides,
                    Optional.of(v2Hides)
            );
        } catch (RuntimeException shadowFailure) {
            return RuntimeDecisionComparison.V2_CANNOT_EVALUATE;
        }
    }

    @FunctionalInterface
    public interface ObservationContextSource {
        List<ObservationContext> collect(
                ServerLevel level,
                ServerPlayer player,
                BlockPos target
        );
    }
}
