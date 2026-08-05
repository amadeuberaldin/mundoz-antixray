package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Optional;

public final class ReplacementRepresentationMapper {

    private ReplacementRepresentationMapper() {}

    public static BlockState map(
            ReplacementRepresentation representation
    ) {
        Objects.requireNonNull(
                representation,
                "representation"
        );

        return switch (representation) {
            case STONE -> Blocks.STONE.defaultBlockState();
            case DEEPSLATE -> Blocks.DEEPSLATE.defaultBlockState();
            case NETHERRACK -> Blocks.NETHERRACK.defaultBlockState();
            case END_STONE -> Blocks.END_STONE.defaultBlockState();
            case TUFF -> Blocks.TUFF.defaultBlockState();
        };
    }

    public static Optional<ReplacementRepresentation> map(
            BlockState state
    ) {
        Objects.requireNonNull(state, "state");

        if (state.is(Blocks.STONE)) {
            return Optional.of(ReplacementRepresentation.STONE);
        }
        if (state.is(Blocks.DEEPSLATE)) {
            return Optional.of(ReplacementRepresentation.DEEPSLATE);
        }
        if (state.is(Blocks.NETHERRACK)) {
            return Optional.of(ReplacementRepresentation.NETHERRACK);
        }
        if (state.is(Blocks.END_STONE)) {
            return Optional.of(ReplacementRepresentation.END_STONE);
        }
        if (state.is(Blocks.TUFF)) {
            return Optional.of(ReplacementRepresentation.TUFF);
        }
        return Optional.empty();
    }
}
