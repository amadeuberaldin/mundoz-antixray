package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Reads possible reveal targets from Minecraft world state without deciding
 * whether the player observes them and without modifying the world.
 */
public final class MinecraftRevealCandidateScanner {

    public static final int REVEAL_RADIUS = 4;

    public List<MinecraftRevealCandidate> scan(
            BlockGetter world,
            BlockPos center
    ) {
        Objects.requireNonNull(
                world,
                "world"
        );

        return scan(
                center,
                world::getBlockState
        );
    }

    List<MinecraftRevealCandidate> scan(
            BlockPos center,
            Function<BlockPos, BlockState> blockStateAt
    ) {
        Objects.requireNonNull(
                center,
                "center"
        );
        Objects.requireNonNull(
                blockStateAt,
                "blockStateAt"
        );

        List<MinecraftRevealCandidate> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();

        for (int y = -REVEAL_RADIUS; y <= REVEAL_RADIUS; y++) {
            for (int z = -REVEAL_RADIUS; z <= REVEAL_RADIUS; z++) {
                for (int x = -REVEAL_RADIUS; x <= REVEAL_RADIUS; x++) {
                    current.setWithOffset(center, x, y, z);

                    BlockIdentityMapper.map(
                            blockStateAt.apply(current)
                    ).ifPresent(block -> candidates.add(
                            new MinecraftRevealCandidate(
                                    BlockPositionMapper.toDomain(current),
                                    block
                            )
                    ));
                }
            }
        }

        return List.copyOf(candidates);
    }
}
