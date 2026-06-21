package com.amadeu.mundozantixray.mixin;

import com.amadeu.mundozantixray.AntiXrayContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.PlayerChunkSender")
public class PlayerChunkSenderMixin {

    @Inject(method = "sendChunk", at = @At("HEAD"))
    private static void mundozAntiXray$setContext(
            ServerGamePacketListenerImpl connection,
            ServerLevel level,
            LevelChunk chunk,
            CallbackInfo ci
    ) {
        ServerPlayer player = connection.player;
        AntiXrayContext.set(player, level, chunk);
    }

    @Inject(method = "sendChunk", at = @At("RETURN"))
    private static void mundozAntiXray$clearContext(
            ServerGamePacketListenerImpl connection,
            ServerLevel level,
            LevelChunk chunk,
            CallbackInfo ci
    ) {
        AntiXrayContext.clear();
    }
}
