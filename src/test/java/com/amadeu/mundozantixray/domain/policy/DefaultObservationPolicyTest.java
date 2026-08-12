package com.amadeu.mundozantixray.domain.policy;

import com.amadeu.mundozantixray.domain.model.BlockPosition;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationDecision;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultObservationPolicyTest {
    private final DefaultObservationPolicy policy = new DefaultObservationPolicy();

    @Test
    void targetIsObservedAcrossAnEmptyOrPassThroughPath() {
        assertEquals(ObservationDecision.OBSERVED, evaluate(List.of()));
        assertEquals(ObservationDecision.OBSERVED,
                evaluate(List.of(ObservationPathBehavior.PASS_THROUGH, ObservationPathBehavior.PASS_THROUGH)));
    }

    @Test
    void stoneAsTheFirstOccludingTargetIsObserved() {
        assertEquals(ObservationDecision.OBSERVED,
                evaluate(List.of(ObservationPathBehavior.PASS_THROUGH, ObservationPathBehavior.PASS_THROUGH)));
    }

    @Test
    void diamondBehindStoneIsNotObserved() {
        assertEquals(ObservationDecision.NOT_OBSERVED,
                evaluate(List.of(ObservationPathBehavior.PASS_THROUGH,
                        ObservationPathBehavior.PASS_THROUGH, ObservationPathBehavior.OCCLUDING)));
    }

    @Test
    void anyOccluderBeforeTheTargetBlocksObservation() {
        assertEquals(ObservationDecision.NOT_OBSERVED,
                evaluate(List.of(ObservationPathBehavior.OCCLUDING, ObservationPathBehavior.PASS_THROUGH)));
    }

    @Test
    void unknownPathInformationFailsVisibleEvenWithOtherOccluders() {
        assertEquals(ObservationDecision.OBSERVED,
                evaluate(List.of(ObservationPathBehavior.OCCLUDING, ObservationPathBehavior.UNKNOWN)));
    }

    @Test
    void nullContextIsRejected() {
        assertThrows(NullPointerException.class, () -> policy.evaluate(null));
    }

    private ObservationDecision evaluate(List<ObservationPathBehavior> path) {
        return policy.evaluate(new ObservationContext(new BlockPosition(0, 0, 0),
                new BlockPosition(path.size() + 1, 0, 0), path));
    }
}
