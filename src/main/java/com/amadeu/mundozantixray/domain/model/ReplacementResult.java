package com.amadeu.mundozantixray.domain.model;

import java.util.Objects;
import java.util.Optional;

public record ReplacementResult(
        ReplacementDecision decision,
        Optional<ReplacementRepresentation> representation
) {

    public ReplacementResult {
        Objects.requireNonNull(
                decision,
                "decision"
        );

        Objects.requireNonNull(
                representation,
                "representation"
        );
    }
}
