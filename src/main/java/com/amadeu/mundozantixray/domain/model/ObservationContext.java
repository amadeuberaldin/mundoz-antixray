package com.amadeu.mundozantixray.domain.model;

import java.util.List;
import java.util.Objects;

public record ObservationContext(
        BlockPosition observerPosition,
        BlockPosition targetPosition,
        List<ObservationPathBehavior> pathBeforeTarget
) {
    public ObservationContext {
        Objects.requireNonNull(observerPosition, "observerPosition");
        Objects.requireNonNull(targetPosition, "targetPosition");
        pathBeforeTarget = List.copyOf(
                Objects.requireNonNull(pathBeforeTarget, "pathBeforeTarget")
        );
    }
}
