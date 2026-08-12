package com.amadeu.mundozantixray.domain.service;

import com.amadeu.mundozantixray.domain.model.BlockPosition;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObservationEvaluationServiceTest {
    private static final ObservationContext CONTEXT = new ObservationContext(
            new BlockPosition(0, 0, 0), new BlockPosition(1, 0, 0), List.of());

    @Test
    void observationDecisionIsPropagatedUnchanged() {
        ObservationEvaluationService service = new ObservationEvaluationService(
                context -> ObservationDecision.NOT_OBSERVED);
        assertSame(ObservationDecision.NOT_OBSERVED, service.evaluate(CONTEXT));
    }

    @Test
    void nullPolicyIsRejected() {
        assertThrows(NullPointerException.class, () -> new ObservationEvaluationService(null));
    }

    @Test
    void nullContextIsRejectedBeforePolicyEvaluation() {
        ObservationEvaluationService service = new ObservationEvaluationService(
                context -> ObservationDecision.OBSERVED);
        assertThrows(NullPointerException.class, () -> service.evaluate(null));
    }
}
