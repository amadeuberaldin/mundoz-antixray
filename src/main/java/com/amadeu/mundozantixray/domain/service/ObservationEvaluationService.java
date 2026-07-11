package com.amadeu.mundozantixray.domain.service;

import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;

public final class ObservationEvaluationService {

    public ObservationDecision evaluate(
            ObservationContext context
    ) {
        return ObservationDecision.OBSERVED;
    }
}
