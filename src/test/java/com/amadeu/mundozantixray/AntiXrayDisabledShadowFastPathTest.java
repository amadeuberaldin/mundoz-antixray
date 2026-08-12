package com.amadeu.mundozantixray;

import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AntiXrayDisabledShadowFastPathTest {
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void clearContext() {
        AntiXrayContext.clear();
    }

    @Test
    void absentStartupPropertyKeepsObfuscatorOutOfShadowRuntime() {
        assertFalse(AntiXrayShadowRuntime.isEnabled());

        LevelChunkSection section = mock(LevelChunkSection.class);
        LevelChunkSection fake = mock(LevelChunkSection.class);
        LevelChunk chunk = mock(LevelChunk.class);
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        FriendlyByteBuf buffer = mock(FriendlyByteBuf.class);

        when(section.maybeHas(any())).thenReturn(true);
        when(section.copy()).thenReturn(fake);
        when(section.getBlockState(anyInt(), anyInt(), anyInt())).thenAnswer(
                invocation -> invocation.getArgument(0, Integer.class) == 0
                        && invocation.getArgument(1, Integer.class) == 0
                        && invocation.getArgument(2, Integer.class) == 0
                        ? Blocks.DIAMOND_ORE.defaultBlockState()
                        : Blocks.AIR.defaultBlockState()
        );
        when(chunk.getSections()).thenReturn(new LevelChunkSection[]{section});
        when(chunk.getMinY()).thenReturn(0);
        when(chunk.getHeight()).thenReturn(16);
        when(chunk.getPos()).thenReturn(new ChunkPos(0, 0));
        when(chunk.getLevel()).thenReturn(level);
        when(level.getBlockState(any())).thenReturn(
                Blocks.STONE.defaultBlockState()
        );
        AntiXrayContext.set(player, level, chunk);

        try (MockedStatic<AntiXrayShadowRuntime> shadowRuntime =
                     mockStatic(AntiXrayShadowRuntime.class, CALLS_REAL_METHODS)) {
            AntiXrayObfuscator.writeSection(section, buffer);

            shadowRuntime.verify(
                    () -> AntiXrayShadowRuntime.beginSection(section),
                    never()
            );
        }

        verify(fake).write(buffer);
    }
}
