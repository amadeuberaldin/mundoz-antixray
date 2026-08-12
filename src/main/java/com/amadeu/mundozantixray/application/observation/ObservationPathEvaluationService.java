package com.amadeu.mundozantixray.application.observation;

import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.service.ObservationEvaluationService;

import java.util.List;
import java.util.Objects;

public final class ObservationPathEvaluationService {
    private final ObservationEvaluationService observationService;

    public ObservationPathEvaluationService(
            ObservationEvaluationService observationService
    ) {
        this.observationService = Objects.requireNonNull(
                observationService,
                "observationService"
        );
    }

    public ObservationDecision evaluate(List<ObservationContext> sampleContexts) {
        List<ObservationContext> contexts = List.copyOf(
                Objects.requireNonNull(sampleContexts, "sampleContexts")
        );
        if (contexts.isEmpty()) {
            throw new IllegalArgumentException("sampleContexts must not be empty");
        }

        for (ObservationContext context : contexts) {
            if (observationService.evaluate(context) == ObservationDecision.OBSERVED) {
                return ObservationDecision.OBSERVED;
            }
        }

        return ObservationDecision.NOT_OBSERVED;
    }
}
