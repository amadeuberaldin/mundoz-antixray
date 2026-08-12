package com.amadeu.mundozantixray.application.runtime;

import java.util.Objects;
import java.util.Optional;

public enum RuntimeDecisionComparison {
    BOTH_REVEAL,
    BOTH_HIDE,
    V1_HIDES_V2_REVEALS,
    V1_REVEALS_V2_HIDES,
    V2_CANNOT_EVALUATE;

    public static RuntimeDecisionComparison compare(
            boolean v1Hides,
            Optional<Boolean> v2Hides
    ) {
        Objects.requireNonNull(v2Hides, "v2Hides");

        if (v2Hides.isEmpty()) {
            return V2_CANNOT_EVALUATE;
        }

        if (v1Hides) {
            return v2Hides.orElseThrow()
                    ? BOTH_HIDE
                    : V1_HIDES_V2_REVEALS;
        }

        return v2Hides.orElseThrow()
                ? V1_REVEALS_V2_HIDES
                : BOTH_REVEAL;
    }
}
