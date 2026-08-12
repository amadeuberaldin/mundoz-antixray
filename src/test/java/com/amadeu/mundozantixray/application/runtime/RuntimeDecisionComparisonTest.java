package com.amadeu.mundozantixray.application.runtime;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuntimeDecisionComparisonTest {

    @Test
    void classifiesEveryComparableDecisionPair() {
        assertEquals(
                RuntimeDecisionComparison.BOTH_REVEAL,
                RuntimeDecisionComparison.compare(false, Optional.of(false))
        );
        assertEquals(
                RuntimeDecisionComparison.BOTH_HIDE,
                RuntimeDecisionComparison.compare(true, Optional.of(true))
        );
        assertEquals(
                RuntimeDecisionComparison.V1_HIDES_V2_REVEALS,
                RuntimeDecisionComparison.compare(true, Optional.of(false))
        );
        assertEquals(
                RuntimeDecisionComparison.V1_REVEALS_V2_HIDES,
                RuntimeDecisionComparison.compare(false, Optional.of(true))
        );
    }

    @Test
    void unavailableV2DecisionCannotBeCompared() {
        assertEquals(
                RuntimeDecisionComparison.V2_CANNOT_EVALUATE,
                RuntimeDecisionComparison.compare(false, Optional.empty())
        );
        assertEquals(
                RuntimeDecisionComparison.V2_CANNOT_EVALUATE,
                RuntimeDecisionComparison.compare(true, Optional.empty())
        );
    }

    @Test
    void nullV2DecisionIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> RuntimeDecisionComparison.compare(false, null)
        );
    }
}
