package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.BlockPosition;

import java.util.Objects;

public record MinecraftRevealCandidate(
        BlockPosition position,
        BlockIdentity block
) {
    public MinecraftRevealCandidate {
        Objects.requireNonNull(
                position,
                "position"
        );
        Objects.requireNonNull(
                block,
                "block"
        );
    }
}
