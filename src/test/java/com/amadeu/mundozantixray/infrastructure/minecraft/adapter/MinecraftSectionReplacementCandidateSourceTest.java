package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinecraftSectionReplacementCandidateSourceTest {

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private final MinecraftSectionReplacementCandidateSource source =
            new MinecraftSectionReplacementCandidateSource();

    @Test
    void returnsAcceptedSectionStatesInReplacementPriorityOrder() {
        Set<BlockState> sectionStates = Set.of(
                Blocks.TUFF.defaultBlockState(),
                Blocks.DIAMOND_ORE.defaultBlockState(),
                Blocks.NETHERRACK.defaultBlockState(),
                Blocks.STONE.defaultBlockState()
        );

        assertEquals(
                List.of(
                        Blocks.STONE.defaultBlockState(),
                        Blocks.NETHERRACK.defaultBlockState(),
                        Blocks.TUFF.defaultBlockState()
                ),
                source.candidatesMatching(sectionStates::contains)
        );
    }

    @Test
    void rejectsUnsafeAndUnsupportedSectionStates() {
        Set<BlockState> sectionStates = Set.of(
                Blocks.AIR.defaultBlockState(),
                Blocks.DIAMOND_ORE.defaultBlockState(),
                Blocks.BEDROCK.defaultBlockState(),
                Blocks.BARRIER.defaultBlockState(),
                Blocks.OBSIDIAN.defaultBlockState(),
                Blocks.CRYING_OBSIDIAN.defaultBlockState(),
                Blocks.OAK_PLANKS.defaultBlockState()
        );

        assertEquals(
                List.of(),
                source.candidatesMatching(sectionStates::contains)
        );
    }

    @Test
    void resultIsImmutable() {
        List<BlockState> candidates = source.candidatesMatching(
                Blocks.STONE.defaultBlockState()::equals
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> candidates.add(Blocks.TUFF.defaultBlockState())
        );
    }

    @Test
    void nullInputsAreRejected() {
        assertThrows(
                NullPointerException.class,
                () -> source.candidatesFrom(null)
        );
        assertThrows(
                NullPointerException.class,
                () -> source.candidatesMatching(null)
        );
    }
}
