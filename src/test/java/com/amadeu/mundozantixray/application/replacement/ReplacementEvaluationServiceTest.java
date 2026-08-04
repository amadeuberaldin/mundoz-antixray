package com.amadeu.mundozantixray.application.replacement;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import com.amadeu.mundozantixray.domain.model.ReplacementResult;
import com.amadeu.mundozantixray.domain.replacement.ReplacementContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplacementEvaluationServiceTest {

    private static final ReplacementContext CONTEXT =
            new ReplacementContext(
                    BlockIdentity.DIAMOND_ORE,
                    List.of(ReplacementRepresentation.STONE)
            );

    @Test
    void replacementResultIsPropagatedUnchanged() {
        ReplacementResult expected = ReplacementResult.replaceWith(
                ReplacementRepresentation.STONE
        );
        ReplacementEvaluationService service =
                new ReplacementEvaluationService(context -> expected);

        ReplacementResult actual = service.evaluate(CONTEXT);

        assertSame(expected, actual);
    }

    @Test
    void keepVisibleResultIsPropagatedUnchanged() {
        ReplacementResult expected = ReplacementResult.keepVisible();
        ReplacementEvaluationService service =
                new ReplacementEvaluationService(context -> expected);

        assertEquals(
                expected,
                service.evaluate(CONTEXT)
        );
    }

    @Test
    void nullPolicyIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new ReplacementEvaluationService(null)
        );
    }

    @Test
    void nullContextIsRejectedBeforePolicyEvaluation() {
        ReplacementEvaluationService service =
                new ReplacementEvaluationService(
                        context -> ReplacementResult.keepVisible()
                );

        assertThrows(
                NullPointerException.class,
                () -> service.evaluate(null)
        );
    }
}
