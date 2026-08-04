package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.BlockPosition;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftRevealCandidateScannerTest {

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private final MinecraftRevealCandidateScanner scanner =
            new MinecraftRevealCandidateScanner();

    @Test
    void scansTheCompleteRadiusFourCube() {
        AtomicInteger reads = new AtomicInteger();

        List<MinecraftRevealCandidate> candidates = scanner.scan(
                new BlockPos(10, 20, 30),
                position -> {
                    reads.incrementAndGet();
                    return Blocks.STONE.defaultBlockState();
                }
        );

        assertEquals(729, reads.get());
        assertTrue(candidates.isEmpty());
    }

    @Test
    void mapsKnownBlocksAtInclusiveScanBoundaries() {
        BlockPos center = new BlockPos(10, 20, 30);
        BlockPos minimum = new BlockPos(6, 16, 26);
        BlockPos maximum = new BlockPos(14, 24, 34);

        List<MinecraftRevealCandidate> candidates = scanner.scan(
                center,
                position -> {
                    if (position.equals(minimum)) {
                        return Blocks.DIAMOND_ORE.defaultBlockState();
                    }
                    if (position.equals(maximum)) {
                        return Blocks.ANCIENT_DEBRIS.defaultBlockState();
                    }
                    return Blocks.STONE.defaultBlockState();
                }
        );

        assertEquals(
                List.of(
                        new MinecraftRevealCandidate(
                                new BlockPosition(6, 16, 26),
                                BlockIdentity.DIAMOND_ORE
                        ),
                        new MinecraftRevealCandidate(
                                new BlockPosition(14, 24, 34),
                                BlockIdentity.ANCIENT_DEBRIS
                        )
                ),
                candidates
        );
    }

    @Test
    void ignoresUnsupportedBlocks() {
        List<MinecraftRevealCandidate> candidates = scanner.scan(
                BlockPos.ZERO,
                position -> position.equals(BlockPos.ZERO)
                        ? Blocks.AMETHYST_BLOCK.defaultBlockState()
                        : Blocks.STONE.defaultBlockState()
        );

        assertTrue(candidates.isEmpty());
    }

    @Test
    void returnedCandidatesAreImmutable() {
        List<MinecraftRevealCandidate> candidates = scanner.scan(
                BlockPos.ZERO,
                position -> position.equals(BlockPos.ZERO)
                        ? Blocks.DIAMOND_ORE.defaultBlockState()
                        : Blocks.STONE.defaultBlockState()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> candidates.add(
                        new MinecraftRevealCandidate(
                                new BlockPosition(1, 1, 1),
                                BlockIdentity.LAVA
                        )
                )
        );
    }

    @Test
    void nullInputsAreRejected() {
        assertThrows(
                NullPointerException.class,
                () -> scanner.scan(
                        null,
                        position -> Blocks.STONE.defaultBlockState()
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> scanner.scan(
                        BlockPos.ZERO,
                        null
                )
        );
    }
}
