package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Converts Minecraft block states into domain block identities.
 *
 * This class belongs to the Minecraft infrastructure layer.
 * The domain must never depend on Minecraft classes.
 */
public final class BlockIdentityMapper {

    private BlockIdentityMapper() {}

    public static Optional<BlockIdentity> map(
            BlockState state
    ) {
        if (state.is(Blocks.DIAMOND_ORE)) {
            return Optional.of(BlockIdentity.DIAMOND_ORE);
        }

        if (state.is(Blocks.ANCIENT_DEBRIS)) {
            return Optional.of(BlockIdentity.ANCIENT_DEBRIS);
        }

        if (state.is(Blocks.LAVA)) {
            return Optional.of(BlockIdentity.LAVA);
        }

        return Optional.empty();
    }
}
