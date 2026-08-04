package com.amadeu.mundozantixray.application.protection;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProtectionEvaluationServiceTest {

    @Test
    void protectedDecisionIsPropagatedUnchanged() {
        ProtectionEvaluationService service = new ProtectionEvaluationService(
                block -> ProtectionDecision.PROTECTED
        );

        assertEquals(
                ProtectionDecision.PROTECTED,
                service.evaluate(BlockIdentity.DIAMOND_ORE)
        );
    }

    @Test
    void notProtectedDecisionIsPropagatedUnchanged() {
        ProtectionEvaluationService service = new ProtectionEvaluationService(
                block -> ProtectionDecision.NOT_PROTECTED
        );

        assertEquals(
                ProtectionDecision.NOT_PROTECTED,
                service.evaluate(BlockIdentity.COAL_ORE)
        );
    }

    @Test
    void nullPolicyIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new ProtectionEvaluationService(null)
        );
    }

    @Test
    void nullIdentityIsRejectedBeforePolicyEvaluation() {
        ProtectionEvaluationService service = new ProtectionEvaluationService(
                block -> ProtectionDecision.PROTECTED
        );

        assertThrows(
                NullPointerException.class,
                () -> service.evaluate(null)
        );
    }
}
