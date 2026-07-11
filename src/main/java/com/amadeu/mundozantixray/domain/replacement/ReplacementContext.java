package com.amadeu.mundozantixray.domain.replacement;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;

import java.util.Objects;

public record ReplacementContext(
        BlockIdentity hiddenBlock
) {
    public ReplacementContext {
        Objects.requireNonNull(
                hiddenBlock,
                "hiddenBlock"
        );
    }
}
