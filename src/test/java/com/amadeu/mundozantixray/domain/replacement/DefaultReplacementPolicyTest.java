package com.amadeu.mundozantixray.domain.replacement;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ReplacementDecision;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import com.amadeu.mundozantixray.domain.model.ReplacementResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultReplacementPolicyTest {

    private final DefaultReplacementPolicy policy =
            new DefaultReplacementPolicy();

    @Test
    void selectsAvailableRepresentationInV1PriorityOrder() {
        ReplacementContext context = new ReplacementContext(
                BlockIdentity.DIAMOND_ORE,
                List.of(
                        ReplacementRepresentation.TUFF,
                        ReplacementRepresentation.NETHERRACK,
                        ReplacementRepresentation.STONE
                )
        );

        ReplacementResult result = policy.evaluate(context);

        assertEquals(
                ReplacementRepresentation.STONE,
                result.representation().orElseThrow()
        );
    }

    @Test
    void everySupportedTerrainRepresentationCanBeSelected() {
        for (ReplacementRepresentation representation
                : ReplacementRepresentation.values()) {
            ReplacementResult result = policy.evaluate(
                    new ReplacementContext(
                            BlockIdentity.DIAMOND_ORE,
                            List.of(representation)
                    )
            );

            assertEquals(
                    ReplacementDecision.REPLACE,
                    result.decision(),
                    representation.name()
            );
            assertEquals(
                    representation,
                    result.representation().orElseThrow(),
                    representation.name()
            );
        }
    }

    @Test
    void keepsOriginalVisibleWhenNoSafeRepresentationIsAvailable() {
        ReplacementResult result = policy.evaluate(
                new ReplacementContext(
                        BlockIdentity.DIAMOND_ORE,
                        List.of()
                )
        );

        assertEquals(
                ReplacementDecision.KEEP_VISIBLE,
                result.decision()
        );
        assertTrue(result.representation().isEmpty());
    }

    @Test
    void nullContextIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> policy.evaluate(null)
        );
    }
}
