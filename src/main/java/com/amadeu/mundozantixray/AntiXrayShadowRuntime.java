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

final class AntiXrayShadowRuntime {
    static final String ENABLED_PROPERTY = "mundoz.antixray.v2-shadow";

    private static final boolean ENABLED = Boolean.getBoolean(ENABLED_PROPERTY);
    private static final MinecraftRuntimeShadowEvaluator EVALUATOR = evaluator();
    private static final MinecraftSectionReplacementCandidateSource CANDIDATE_SOURCE =
            new MinecraftSectionReplacementCandidateSource();
    private static final ReplacementContextMapper CONTEXT_MAPPER =
            new ReplacementContextMapper();

    private AntiXrayShadowRuntime() {}

    static SectionEvaluation beginSection(LevelChunkSection section) {
        if (!ENABLED) {
            return SectionEvaluation.disabled();
        }

        try {
            List<ReplacementRepresentation> representations =
                    CONTEXT_MAPPER.mapAvailableRepresentations(
                            CANDIDATE_SOURCE.candidatesFrom(section)
                    );
            return new SectionEvaluation(EVALUATOR, representations);
        } catch (RuntimeException shadowFailure) {
            return SectionEvaluation.disabled();
        }
    }

    static final class SectionEvaluation {
        private final MinecraftRuntimeShadowEvaluator evaluator;
        private final List<ReplacementRepresentation> representations;

        private SectionEvaluation(
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
                BlockPos target,
                BlockState state,
                boolean v1Hides
        ) {
            if (evaluator == null) {
                return RuntimeDecisionComparison.V2_CANNOT_EVALUATE;
            }

            return evaluator.compare(
                    level,
                    player,
                    target,
                    state,
                    representations,
                    v1Hides
            );
        }
    }

    private static MinecraftRuntimeShadowEvaluator evaluator() {
        return new MinecraftRuntimeShadowEvaluator(
                new MinecraftObservationPathCollector(
                        new MinecraftObservationTargetSampler(),
                        new MinecraftObservationPathClassifier()
                ),
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
