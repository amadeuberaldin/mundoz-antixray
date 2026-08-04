package com.amadeu.mundozantixray.domain.reveal;

import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;

import java.util.Objects;

public record RevealContext(
        ProtectionDecision protectionDecision,
        ObservationDecision observationDecision
) {
    public RevealContext {
        Objects.requireNonNull(
                protectionDecision,
                "protectionDecision"
        );
        Objects.requireNonNull(
                observationDecision,
                "observationDecision"
        );
    }
}
