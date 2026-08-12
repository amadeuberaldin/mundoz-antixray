package com.amadeu.mundozantixray.domain.service;

import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.policy.ObservationPolicy;

import java.util.Objects;

public final class ObservationEvaluationService {
    private final ObservationPolicy policy;

    public ObservationEvaluationService(ObservationPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public ObservationDecision evaluate(
            ObservationContext context
    ) {
        Objects.requireNonNull(context, "context");
        return policy.evaluate(context);
    }
}
