package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

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
}
