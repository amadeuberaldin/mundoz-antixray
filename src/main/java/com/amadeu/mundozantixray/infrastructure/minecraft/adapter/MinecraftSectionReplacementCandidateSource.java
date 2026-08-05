package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Reads accepted replacement candidates from a chunk section without copying,
 * modifying, or serializing the section.
 */
public final class MinecraftSectionReplacementCandidateSource {

    private static final List<BlockState> ACCEPTED_STATES = List.of(
            Blocks.STONE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.NETHERRACK.defaultBlockState(),
            Blocks.END_STONE.defaultBlockState(),
            Blocks.TUFF.defaultBlockState()
    );

    public List<BlockState> candidatesFrom(
            LevelChunkSection section
    ) {
        Objects.requireNonNull(section, "section");
        return candidatesMatching(
                state -> section.maybeHas(state::equals)
        );
    }

    List<BlockState> candidatesMatching(
            Predicate<BlockState> sectionContains
    ) {
        Objects.requireNonNull(sectionContains, "sectionContains");

        List<BlockState> candidates = new ArrayList<>();
        for (BlockState acceptedState : ACCEPTED_STATES) {
            if (sectionContains.test(acceptedState)) {
                candidates.add(acceptedState);
            }
        }
        return List.copyOf(candidates);
    }
}
