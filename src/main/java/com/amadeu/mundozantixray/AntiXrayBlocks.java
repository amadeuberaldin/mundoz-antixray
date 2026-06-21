package com.amadeu.mundozantixray;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AntiXrayBlocks {

    private AntiXrayBlocks() {}

    public static boolean isHiddenOre(BlockState state) {
        return isOreLike(state) || isStructureLike(state);
    }

    public static boolean isOreLike(BlockState state) {
        Block block = state.getBlock();

        return block == Blocks.COAL_ORE
                || block == Blocks.DEEPSLATE_COAL_ORE
                || block == Blocks.COPPER_ORE
                || block == Blocks.DEEPSLATE_COPPER_ORE
                || block == Blocks.IRON_ORE
                || block == Blocks.DEEPSLATE_IRON_ORE
                || block == Blocks.GOLD_ORE
                || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.REDSTONE_ORE
                || block == Blocks.DEEPSLATE_REDSTONE_ORE
                || block == Blocks.EMERALD_ORE
                || block == Blocks.DEEPSLATE_EMERALD_ORE
                || block == Blocks.LAPIS_ORE
                || block == Blocks.DEEPSLATE_LAPIS_ORE
                || block == Blocks.DIAMOND_ORE
                || block == Blocks.DEEPSLATE_DIAMOND_ORE
                || block == Blocks.NETHER_GOLD_ORE
                || block == Blocks.NETHER_QUARTZ_ORE
                || block == Blocks.ANCIENT_DEBRIS
                || block == Blocks.AMETHYST_BLOCK
                || block == Blocks.BUDDING_AMETHYST
                || block == Blocks.AMETHYST_CLUSTER
                || block == Blocks.LARGE_AMETHYST_BUD
                || block == Blocks.MEDIUM_AMETHYST_BUD
                || block == Blocks.SMALL_AMETHYST_BUD
                || block == Blocks.CALCITE
                || block == Blocks.SMOOTH_BASALT
                || block == Blocks.LAVA;
    }

    public static boolean isStructureLike(BlockState state) {
        Block block = state.getBlock();

        return block == Blocks.TUFF_SLAB;
    }

    public static BlockState replacementFor(ServerLevel level, BlockState original) {
        return Blocks.STONE.defaultBlockState();
    }
}
