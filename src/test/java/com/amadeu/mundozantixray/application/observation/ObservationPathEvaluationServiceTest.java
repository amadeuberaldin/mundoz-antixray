package com.amadeu.mundozantixray.application.observation;

import com.amadeu.mundozantixray.domain.model.BlockPosition;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import com.amadeu.mundozantixray.domain.policy.DefaultObservationPolicy;
import com.amadeu.mundozantixray.domain.service.ObservationEvaluationService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObservationPathEvaluationServiceTest {
    private final ObservationPathEvaluationService service =
            new ObservationPathEvaluationService(
                    new ObservationEvaluationService(
                            new DefaultObservationPolicy()
                    )
            );

    @Test
    void anyObservedSampleMakesTheTargetObserved() {
        assertEquals(
                ObservationDecision.OBSERVED,
                service.evaluate(List.of(
                        context(ObservationPathBehavior.OCCLUDING),
                        context(ObservationPathBehavior.PASS_THROUGH),
                        context(ObservationPathBehavior.OCCLUDING)
                ))
        );
    }

    @Test
    void targetIsNotObservedOnlyWhenEverySampleIsNotObserved() {
        assertEquals(
                ObservationDecision.NOT_OBSERVED,
                service.evaluate(List.of(
                        context(ObservationPathBehavior.OCCLUDING),
                        context(ObservationPathBehavior.PASS_THROUGH,
                                ObservationPathBehavior.OCCLUDING),
                        context(ObservationPathBehavior.OCCLUDING)
                ))
        );
    }

    @Test
    void unknownSampleFailsTheAggregateVisible() {
        assertEquals(
                ObservationDecision.OBSERVED,
                service.evaluate(List.of(
                        context(ObservationPathBehavior.OCCLUDING),
                        context(ObservationPathBehavior.UNKNOWN)
                ))
        );
    }

    @Test
    void invalidInputsAreRejected() {
        assertThrows(NullPointerException.class,
                () -> new ObservationPathEvaluationService(null));
        assertThrows(NullPointerException.class, () -> service.evaluate(null));
        assertThrows(IllegalArgumentException.class,
                () -> service.evaluate(List.of()));
        assertThrows(NullPointerException.class,
                () -> service.evaluate(Arrays.asList(context(), null)));
    }

    private static ObservationContext context(
            ObservationPathBehavior... path
    ) {
        return new ObservationContext(
                new BlockPosition(0, 0, 0),
                new BlockPosition(3, 0, 0),
                List.of(path)
        );
    }
}
