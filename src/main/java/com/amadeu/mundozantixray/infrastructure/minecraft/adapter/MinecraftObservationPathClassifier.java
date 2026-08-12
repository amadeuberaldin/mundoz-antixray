package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public final class MinecraftObservationPathClassifier {
    public ObservationPathBehavior classify(BlockState state) {
        Objects.requireNonNull(state, "state");

        return isPassThrough(state)
                ? ObservationPathBehavior.PASS_THROUGH
                : ObservationPathBehavior.OCCLUDING;
    }

    private static boolean isPassThrough(BlockState state) {
        Block block = state.getBlock();

        return state.isAir()
                || state.is(Blocks.WATER)
                || block == Blocks.GLASS
                || block instanceof StainedGlassBlock
                || block instanceof TintedGlassBlock
                || block == Blocks.GLASS_PANE
                || block instanceof StainedGlassPaneBlock
                || block == Blocks.IRON_BARS
                || Blocks.COPPER_BARS.asList().contains(block)
                || Blocks.COPPER_GRATE.asList().contains(block)
                || state.is(Blocks.CRAFTING_TABLE)
                || state.is(Blocks.BREWING_STAND)
                || block instanceof DoorBlock
                || block instanceof TrapDoorBlock
                || block instanceof FenceBlock
                || block instanceof FenceGateBlock
                || block instanceof ChainBlock
                || block instanceof LanternBlock
                || block instanceof ButtonBlock
                || state.is(Blocks.LEVER)
                || block instanceof StairBlock;
    }
}
