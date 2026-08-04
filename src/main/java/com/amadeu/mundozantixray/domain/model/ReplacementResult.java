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

        if (decision == ReplacementDecision.REPLACE && representation.isEmpty()) {
            throw new IllegalArgumentException(
                    "REPLACE requires a representation"
            );
        }

        if (decision == ReplacementDecision.KEEP_VISIBLE && representation.isPresent()) {
            throw new IllegalArgumentException(
                    "KEEP_VISIBLE cannot have a representation"
            );
        }
    }

    public static ReplacementResult replaceWith(
            ReplacementRepresentation representation
    ) {
        return new ReplacementResult(
                ReplacementDecision.REPLACE,
                Optional.of(
                        Objects.requireNonNull(
                                representation,
                                "representation"
                        )
                )
        );
    }

    public static ReplacementResult keepVisible() {
        return new ReplacementResult(
                ReplacementDecision.KEEP_VISIBLE,
                Optional.empty()
        );
    }
}
