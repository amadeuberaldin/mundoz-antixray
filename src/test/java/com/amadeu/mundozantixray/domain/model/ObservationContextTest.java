package com.amadeu.mundozantixray.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObservationContextTest {
    @Test
    void pathFactsAreCopiedAndExposedAsImmutable() {
        List<ObservationPathBehavior> path = new ArrayList<>(List.of(ObservationPathBehavior.PASS_THROUGH));
        ObservationContext context = context(path);
        path.add(ObservationPathBehavior.OCCLUDING);

        assertEquals(List.of(ObservationPathBehavior.PASS_THROUGH), context.pathBeforeTarget());
        assertThrows(UnsupportedOperationException.class,
                () -> context.pathBeforeTarget().add(ObservationPathBehavior.OCCLUDING));
    }

    @Test
    void nullInputsAreRejected() {
        BlockPosition position = new BlockPosition(0, 0, 0);
        assertThrows(NullPointerException.class, () -> new ObservationContext(null, position, List.of()));
        assertThrows(NullPointerException.class, () -> new ObservationContext(position, null, List.of()));
        assertThrows(NullPointerException.class, () -> new ObservationContext(position, position, null));
        assertThrows(NullPointerException.class,
                () -> context(Arrays.asList(ObservationPathBehavior.PASS_THROUGH, null)));
    }

    private static ObservationContext context(List<ObservationPathBehavior> path) {
        return new ObservationContext(new BlockPosition(0, 0, 0), new BlockPosition(3, 0, 0), path);
    }
}
