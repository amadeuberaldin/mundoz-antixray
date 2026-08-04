package com.amadeu.mundozantixray.domain.reveal;

import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;
import com.amadeu.mundozantixray.domain.model.RevealDecision;

import java.util.Objects;

public final class DefaultRevealPolicy
        implements RevealPolicy {

    @Override
    public RevealDecision evaluate(
            RevealContext context
    ) {
        Objects.requireNonNull(
                context,
                "context"
        );

        if (context.protectionDecision() == ProtectionDecision.PROTECTED
                && context.observationDecision() == ObservationDecision.OBSERVED) {
            return RevealDecision.REVEAL;
        }

        return RevealDecision.KEEP_HIDDEN;
    }
}
