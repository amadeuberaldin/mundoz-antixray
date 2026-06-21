package com.amadeu.mundozantixray.mixin;

import com.amadeu.mundozantixray.AntiXrayObfuscator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientboundLevelChunkPacketData.class)
public class ClientboundLevelChunkPacketDataMixin {

    @Redirect(
            method = "extractChunkData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;write(Lnet/minecraft/network/FriendlyByteBuf;)V"
            )
    )
    private static void mundozAntiXray$writeObfuscatedSection(
            LevelChunkSection section,
            FriendlyByteBuf buf
    ) {
        AntiXrayObfuscator.writeSection(section, buf);
    }
}
