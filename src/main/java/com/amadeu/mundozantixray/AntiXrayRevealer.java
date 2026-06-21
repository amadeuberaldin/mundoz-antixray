package com.amadeu.mundozantixray;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public final class AntiXrayRevealer {

    private static final int REVEAL_RADIUS = 4;

    private AntiXrayRevealer() {}

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerLevel level)) {
                return;
            }

            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            revealAround(level, serverPlayer, pos);
        });
    }

    private static void revealAround(ServerLevel level, ServerPlayer player, BlockPos center) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int y = -REVEAL_RADIUS; y <= REVEAL_RADIUS; y++) {
            for (int z = -REVEAL_RADIUS; z <= REVEAL_RADIUS; z++) {
                for (int x = -REVEAL_RADIUS; x <= REVEAL_RADIUS; x++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);

                    BlockState current = level.getBlockState(mutable);

                    if (!AntiXrayBlocks.isHiddenOre(current)) {
                        continue;
                    }

                    if (!isExposed(level, mutable)) {
                        continue;
                    }

                    player.connection.send(new ClientboundBlockUpdatePacket(mutable.immutable(), current));
                }
            }
        }
    }

    private static boolean isExposed(ServerLevel level, BlockPos pos) {
        return isTransparent(level, pos.getX() + 1, pos.getY(), pos.getZ())
                || isTransparent(level, pos.getX() - 1, pos.getY(), pos.getZ())
                || isTransparent(level, pos.getX(), pos.getY() + 1, pos.getZ())
                || isTransparent(level, pos.getX(), pos.getY() - 1, pos.getZ())
                || isTransparent(level, pos.getX(), pos.getY(), pos.getZ() + 1)
                || isTransparent(level, pos.getX(), pos.getY(), pos.getZ() - 1);
    }

    private static boolean isTransparent(ServerLevel level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));

        return state.isAir()
                || !state.canOcclude()
                || !state.getFluidState().isEmpty();
    }
}
