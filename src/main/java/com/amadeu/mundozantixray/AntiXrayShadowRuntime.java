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
            () -> System.getProperty(ENABLED_PROPERTY)
    );

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
        try {
            List<ReplacementRepresentation> representations =
                    new ReplacementContextMapper().mapAvailableRepresentations(
                            new MinecraftSectionReplacementCandidateSource()
                                    .candidatesFrom(section)
                    );
            return new SectionEvaluation(evaluator(), representations);
        } catch (Throwable shadowFailure) {
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

            try {
                if (!evaluator.supports(state)) {
                    return RuntimeDecisionComparison.V2_CANNOT_EVALUATE;
                }

                candidateEvaluated = true;
                return evaluator.compare(
                        level,
                        player,
                        new BlockPos(targetX, targetY, targetZ),
                        state,
                        representations,
                        v1Hides
                );
            } catch (Throwable shadowFailure) {
                return RuntimeDecisionComparison.V2_CANNOT_EVALUATE;
            }
        }
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
