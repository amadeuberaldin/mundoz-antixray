package com.amadeu.mundozantixray;

import com.amadeu.mundozantixray.application.runtime.RuntimeDecisionComparison;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowRuntimeDiagnosticsTest {
    @AfterEach
    void clearProperty() {
        System.clearProperty(ShadowRuntimeDiagnostics.ENABLED_PROPERTY);
    }

    @Test
    void absentStartupPropertyDisablesProductionDiagnostics() {
        assertFalse(ShadowRuntimeDiagnostics.isEnabled());
    }

    @Test
    void parsesStartupActivationValues() {
        assertFalse(ShadowRuntimeDiagnostics.parseEnabled(null));
        assertFalse(ShadowRuntimeDiagnostics.parseEnabled("false"));
        assertTrue(ShadowRuntimeDiagnostics.parseEnabled("true"));
    }

    @Test
    void propertyReadFailureResolvesToDisabled() {
        assertFalse(ShadowRuntimeDiagnostics.resolveEnabled(() -> {
            throw new SecurityException("property unavailable");
        }));
    }

    @Test
    void productionActivationIsImmutableAfterInitialization() {
        boolean startupDecision = ShadowRuntimeDiagnostics.isEnabled();
        System.setProperty(ShadowRuntimeDiagnostics.ENABLED_PROPERTY,
                Boolean.toString(!startupDecision));
        assertEquals(startupDecision, ShadowRuntimeDiagnostics.isEnabled());
    }

    @Test
    void aggregatesOnlyBoundedCountersAndTimings() {
        ShadowRuntimeDiagnostics.Aggregate aggregate = new ShadowRuntimeDiagnostics.Aggregate();
        aggregate.recordSectionInitialization(10L);
        aggregate.recordSectionInitialization(20L);
        aggregate.recordComparison(RuntimeDecisionComparison.BOTH_HIDE, 30L);
        aggregate.recordComparison(RuntimeDecisionComparison.V1_HIDES_V2_REVEALS, 40L);
        aggregate.recordComparison(RuntimeDecisionComparison.V1_REVEALS_V2_HIDES, 80L);
        aggregate.recordUnsupported();
        aggregate.recordUnavailable(RuntimeDecisionComparison.BOTH_REVEAL, 50L);
        aggregate.recordMissingReplacement(
                RuntimeDecisionComparison.V1_HIDES_V2_REVEALS, 60L);
        aggregate.recordFailure(70L);

        ShadowRuntimeDiagnostics.Summary summary = aggregate.snapshot();
        assertEquals(new ShadowRuntimeDiagnostics.Summary(2L, 30L, 20L, 6L, 330L, 80L,
                        1L, 1L, 2L, 1L,
                        2L, 3L, 2L, 3L, 1L, 1L, 1L, 1L),
                summary);
        assertEquals(summary.bothReveal() + summary.bothHide(), summary.agreements());
        assertEquals(summary.v1HidesV2Reveals() + summary.v1RevealsV2Hides(),
                summary.disagreements());
    }
}
