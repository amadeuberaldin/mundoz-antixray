package com.amadeu.mundozantixray.application.runtime;

import com.amadeu.mundozantixray.application.observation.ObservationPathEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementIntegrationService;
import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.model.ReplacementDecision;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import com.amadeu.mundozantixray.domain.model.ReplacementResult;

import java.util.List;
import java.util.Objects;

public final class ShadowEvaluationService {
    private final ObservationPathEvaluationService observationService;
    private final ReplacementIntegrationService replacementService;

    public ShadowEvaluationService(
            ObservationPathEvaluationService observationService,
            ReplacementIntegrationService replacementService
    ) {
        this.observationService = Objects.requireNonNull(
                observationService,
                "observationService"
        );
        this.replacementService = Objects.requireNonNull(
                replacementService,
                "replacementService"
        );
    }

    public boolean shouldHide(
            BlockIdentity block,
            List<ObservationContext> observationContexts,
            List<ReplacementRepresentation> availableRepresentations
    ) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(observationContexts, "observationContexts");
        Objects.requireNonNull(
                availableRepresentations,
                "availableRepresentations"
        );

        if (observationService.evaluate(observationContexts)
                == ObservationDecision.OBSERVED) {
            return false;
        }

        ReplacementResult replacement = replacementService.evaluate(
                block,
                availableRepresentations
        );
        return replacement.decision() == ReplacementDecision.REPLACE;
    }
}
