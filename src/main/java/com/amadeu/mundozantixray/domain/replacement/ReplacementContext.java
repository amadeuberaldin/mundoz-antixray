package com.amadeu.mundozantixray.domain.replacement;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;

import java.util.List;
import java.util.Objects;

public record ReplacementContext(
        BlockIdentity hiddenBlock,
        List<ReplacementRepresentation> availableRepresentations
) {
    public ReplacementContext {
        Objects.requireNonNull(
                hiddenBlock,
                "hiddenBlock"
        );
        availableRepresentations = List.copyOf(
                Objects.requireNonNull(
                        availableRepresentations,
                        "availableRepresentations"
                )
        );
    }
}
