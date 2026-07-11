package com.amadeu.mundozantixray.domain.model;

import java.util.Objects;

public record ObservationContext(
        BlockPosition observerPosition,
        BlockPosition targetPosition
) {
    public ObservationContext {
        Objects.requireNonNull(observerPosition, "observerPosition");
        Objects.requireNonNull(targetPosition, "targetPosition");
    }
}
