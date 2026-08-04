package com.amadeu.mundozantixray.domain.reveal;

import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RevealContextTest {

    @Test
    void nullProtectionDecisionIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new RevealContext(
                        null,
                        ObservationDecision.OBSERVED
                )
        );
    }

    @Test
    void nullObservationDecisionIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new RevealContext(
                        ProtectionDecision.PROTECTED,
                        null
                )
        );
    }
}
