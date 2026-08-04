package com.amadeu.mundozantixray.domain.replacement;

import com.amadeu.mundozantixray.domain.model.ReplacementResult;

public interface ReplacementPolicy {

    ReplacementResult evaluate(
            ReplacementContext context
    );
}
