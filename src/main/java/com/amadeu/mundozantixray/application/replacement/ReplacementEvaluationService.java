package com.amadeu.mundozantixray.application.replacement;

import com.amadeu.mundozantixray.domain.model.ReplacementResult;
import com.amadeu.mundozantixray.domain.replacement.ReplacementContext;
import com.amadeu.mundozantixray.domain.replacement.ReplacementPolicy;

import java.util.Objects;

public final class ReplacementEvaluationService {

    private final ReplacementPolicy policy;

    public ReplacementEvaluationService(
            ReplacementPolicy policy
    ) {
        this.policy = Objects.requireNonNull(
                policy,
                "policy"
        );
    }

    public ReplacementResult evaluate(
            ReplacementContext context
    ) {
        Objects.requireNonNull(
                context,
                "context"
        );

        return policy.evaluate(context);
    }
}
