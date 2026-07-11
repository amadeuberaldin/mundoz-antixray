package com.amadeu.mundozantixray.domain.policy;

import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;

public interface ObservationPolicy {

    ObservationDecision evaluate(
            ObservationContext context
    );
}
