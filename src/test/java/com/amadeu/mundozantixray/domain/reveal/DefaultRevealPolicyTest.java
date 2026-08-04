package com.amadeu.mundozantixray.domain.reveal;

import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;
import com.amadeu.mundozantixray.domain.model.RevealDecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultRevealPolicyTest {

    private final DefaultRevealPolicy policy = new DefaultRevealPolicy();

    @Test
    void protectedObservedInformationIsRevealed() {
        assertEquals(
                RevealDecision.REVEAL,
                policy.evaluate(
                        new RevealContext(
                                ProtectionDecision.PROTECTED,
                                ObservationDecision.OBSERVED
                        )
                )
        );
    }

    @Test
    void protectedUnobservedInformationRemainsHidden() {
        assertEquals(
                RevealDecision.KEEP_HIDDEN,
                policy.evaluate(
                        new RevealContext(
                                ProtectionDecision.PROTECTED,
                                ObservationDecision.NOT_OBSERVED
                        )
                )
        );
    }

    @Test
    void unprotectedInformationIsNotHandledAsAReveal() {
        for (ObservationDecision observationDecision : ObservationDecision.values()) {
            assertEquals(
                    RevealDecision.KEEP_HIDDEN,
                    policy.evaluate(
                            new RevealContext(
                                    ProtectionDecision.NOT_PROTECTED,
                                    observationDecision
                            )
                    )
            );
        }
    }

    @Test
    void nullContextIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> policy.evaluate(null)
        );
    }
}
