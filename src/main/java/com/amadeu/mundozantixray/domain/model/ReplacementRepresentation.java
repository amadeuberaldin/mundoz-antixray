package com.amadeu.mundozantixray.domain.model;

import java.util.Objects;

public record ReplacementRepresentation(
        String identifier
) {

    public ReplacementRepresentation {
        Objects.requireNonNull(
                identifier,
                "identifier"
        );
    }
}
