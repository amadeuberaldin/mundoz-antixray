package com.amadeu.mundozantixray.domain.policy;

import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;

import java.util.Objects;

public final class DefaultObservationPolicy implements ObservationPolicy {
    @Override
    public ObservationDecision evaluate(ObservationContext context) {
        Objects.requireNonNull(context, "context");

        if (context.pathBeforeTarget().contains(ObservationPathBehavior.UNKNOWN)) {
            return ObservationDecision.OBSERVED;
        }

        return context.pathBeforeTarget().contains(ObservationPathBehavior.OCCLUDING)
                ? ObservationDecision.NOT_OBSERVED
                : ObservationDecision.OBSERVED;
    }
}
