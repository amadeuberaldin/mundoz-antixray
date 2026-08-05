package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ReplacementContextMapper {

    public List<ReplacementRepresentation> mapAvailableRepresentations(
            List<BlockState> availableStates
    ) {
        Objects.requireNonNull(availableStates, "availableStates");

        List<ReplacementRepresentation> representations = new ArrayList<>();

        for (BlockState state : availableStates) {
            ReplacementRepresentationMapper.map(
                    Objects.requireNonNull(state, "availableState")
            ).filter(representation -> !representations.contains(representation))
                    .ifPresent(representations::add);
        }

        return List.copyOf(representations);
    }
}
