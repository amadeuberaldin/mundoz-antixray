package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
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
        Objects.requireNonNull(
                state,
                "state"
        );

        if (state.is(Blocks.COAL_ORE)) {
            return Optional.of(BlockIdentity.COAL_ORE);
        }
        if (state.is(Blocks.DEEPSLATE_COAL_ORE)) {
            return Optional.of(BlockIdentity.DEEPSLATE_COAL_ORE);
        }
        if (state.is(Blocks.COPPER_ORE)) {
            return Optional.of(BlockIdentity.COPPER_ORE);
        }
        if (state.is(Blocks.DEEPSLATE_COPPER_ORE)) {
            return Optional.of(BlockIdentity.DEEPSLATE_COPPER_ORE);
        }
        if (state.is(Blocks.IRON_ORE)) {
            return Optional.of(BlockIdentity.IRON_ORE);
        }
        if (state.is(Blocks.DEEPSLATE_IRON_ORE)) {
            return Optional.of(BlockIdentity.DEEPSLATE_IRON_ORE);
        }
        if (state.is(Blocks.GOLD_ORE)) {
            return Optional.of(BlockIdentity.GOLD_ORE);
        }
        if (state.is(Blocks.DEEPSLATE_GOLD_ORE)) {
            return Optional.of(BlockIdentity.DEEPSLATE_GOLD_ORE);
        }
        if (state.is(Blocks.REDSTONE_ORE)) {
            return Optional.of(BlockIdentity.REDSTONE_ORE);
        }
        if (state.is(Blocks.DEEPSLATE_REDSTONE_ORE)) {
            return Optional.of(BlockIdentity.DEEPSLATE_REDSTONE_ORE);
        }
        if (state.is(Blocks.EMERALD_ORE)) {
            return Optional.of(BlockIdentity.EMERALD_ORE);
        }
        if (state.is(Blocks.DEEPSLATE_EMERALD_ORE)) {
            return Optional.of(BlockIdentity.DEEPSLATE_EMERALD_ORE);
        }
        if (state.is(Blocks.LAPIS_ORE)) {
            return Optional.of(BlockIdentity.LAPIS_ORE);
        }
        if (state.is(Blocks.DEEPSLATE_LAPIS_ORE)) {
            return Optional.of(BlockIdentity.DEEPSLATE_LAPIS_ORE);
        }
        if (state.is(Blocks.DIAMOND_ORE)) {
            return Optional.of(BlockIdentity.DIAMOND_ORE);
        }
        if (state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
            return Optional.of(BlockIdentity.DEEPSLATE_DIAMOND_ORE);
        }
        if (state.is(Blocks.NETHER_GOLD_ORE)) {
            return Optional.of(BlockIdentity.NETHER_GOLD_ORE);
        }
        if (state.is(Blocks.NETHER_QUARTZ_ORE)) {
            return Optional.of(BlockIdentity.NETHER_QUARTZ_ORE);
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
