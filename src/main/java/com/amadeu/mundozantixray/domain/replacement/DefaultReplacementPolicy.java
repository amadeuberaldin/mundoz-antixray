package com.amadeu.mundozantixray.domain.replacement;

import com.amadeu.mundozantixray.domain.model.ReplacementDecision;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import com.amadeu.mundozantixray.domain.model.ReplacementResult;

import java.util.Objects;
import java.util.Optional;

public final class DefaultReplacementPolicy
        implements ReplacementPolicy {

    @Override
    public ReplacementResult evaluate(
            ReplacementContext context
    ) {
        Objects.requireNonNull(
                context,
                "context"
        );

        return new ReplacementResult(
                ReplacementDecision.REPLACE,
                Optional.of(
                        new ReplacementRepresentation(
                                "minecraft:stone"
                        )
                )
        );
    }
}
