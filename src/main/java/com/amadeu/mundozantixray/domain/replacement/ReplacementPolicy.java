package com.amadeu.mundozantixray.domain.replacement;

import com.amadeu.mundozantixray.domain.model.ReplacementDecision;

public interface ReplacementPolicy {

    ReplacementDecision evaluate(
            ReplacementContext context
    );
}
