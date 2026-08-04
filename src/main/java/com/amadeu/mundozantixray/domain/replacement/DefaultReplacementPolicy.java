package com.amadeu.mundozantixray.domain.replacement;

import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import com.amadeu.mundozantixray.domain.model.ReplacementResult;

import java.util.List;
import java.util.Objects;

public final class DefaultReplacementPolicy
        implements ReplacementPolicy {

    private static final List<ReplacementRepresentation> PRIORITY = List.of(
            ReplacementRepresentation.STONE,
            ReplacementRepresentation.DEEPSLATE,
            ReplacementRepresentation.NETHERRACK,
            ReplacementRepresentation.END_STONE,
            ReplacementRepresentation.TUFF
    );

    @Override
    public ReplacementResult evaluate(
            ReplacementContext context
    ) {
        Objects.requireNonNull(
                context,
                "context"
        );

        for (ReplacementRepresentation representation : PRIORITY) {
            if (context.availableRepresentations().contains(representation)) {
                return ReplacementResult.replaceWith(representation);
            }
        }

        return ReplacementResult.keepVisible();
    }
}
