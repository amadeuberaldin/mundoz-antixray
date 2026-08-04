package com.amadeu.mundozantixray.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplacementResultTest {

    @Test
    void replaceWithCreatesACompleteReplacementResult() {
        ReplacementResult result = ReplacementResult.replaceWith(
                ReplacementRepresentation.STONE
        );

        assertEquals(
                ReplacementDecision.REPLACE,
                result.decision()
        );
        assertEquals(
                ReplacementRepresentation.STONE,
                result.representation().orElseThrow()
        );
    }

    @Test
    void keepVisibleCreatesAResultWithoutRepresentation() {
        ReplacementResult result = ReplacementResult.keepVisible();

        assertEquals(
                ReplacementDecision.KEEP_VISIBLE,
                result.decision()
        );
        assertTrue(result.representation().isEmpty());
    }

    @Test
    void replaceWithoutRepresentationIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ReplacementResult(
                        ReplacementDecision.REPLACE,
                        Optional.empty()
                )
        );
    }

    @Test
    void keepVisibleWithRepresentationIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ReplacementResult(
                        ReplacementDecision.KEEP_VISIBLE,
                        Optional.of(ReplacementRepresentation.STONE)
                )
        );
    }

    @Test
    void nullComponentsAreRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new ReplacementResult(
                        null,
                        Optional.empty()
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> new ReplacementResult(
                        ReplacementDecision.KEEP_VISIBLE,
                        null
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> ReplacementResult.replaceWith(null)
        );
    }
}
