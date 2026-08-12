package com.amadeu.mundozantixray;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AntiXrayEnabledShadowIsolationTest {
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void startupActivationIsEnabledForThisSuite() {
        assertTrue(AntiXrayShadowRuntime.isEnabled());
    }

    @Test
    void unavailableChunkInformationCannotChangeV1OutputOrSectionState() {
        LevelChunkSection section = sectionWith(
                Blocks.STONE.defaultBlockState()
        );
        RuntimeFixture fixture = runtimeFor(section);
        Entity camera = mock(Entity.class);
        when(fixture.player().getCamera()).thenReturn(camera);
        when(camera.getEyePosition()).thenReturn(new Vec3(-2.0D, 0.5D, 0.5D));

        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(fixture.level().getChunkSource()).thenReturn(chunkSource);
        when(chunkSource.getChunkNow(any(Integer.class), any(Integer.class)))
                .thenReturn(null);

        byte[] disabled = serialize(section, fixture, false);
        byte[] enabled = serialize(section, fixture, true);

        assertArrayEquals(disabled, enabled);
        assertSame(
                Blocks.DIAMOND_ORE.defaultBlockState(),
                section.getBlockState(0, 0, 0)
        );
    }

    @Test
    void shadowExceptionCannotAbortOrChangeV1Output() {
        LevelChunkSection section = sectionWith(
                Blocks.STONE.defaultBlockState()
        );
        RuntimeFixture fixture = runtimeFor(section);
        when(fixture.player().getCamera()).thenThrow(
                new IllegalStateException("camera unavailable")
        );

        byte[] disabled = serialize(section, fixture, false);
        byte[] enabled = assertDoesNotThrow(
                () -> serialize(section, fixture, true)
        );

        assertArrayEquals(disabled, enabled);
        assertSame(
                Blocks.DIAMOND_ORE.defaultBlockState(),
                section.getBlockState(0, 0, 0)
        );
    }

    @Test
    void missingV2ReplacementCannotChangeV1Output() {
        LevelChunkSection section = sectionWith(
                Blocks.STONE_BRICKS.defaultBlockState()
        );
        RuntimeFixture fixture = runtimeFor(section);
        Entity camera = mock(Entity.class);
        when(fixture.player().getCamera()).thenReturn(camera);
        when(camera.getEyePosition()).thenReturn(new Vec3(-2.0D, 0.5D, 0.5D));

        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(fixture.level().getChunkSource()).thenReturn(chunkSource);
        when(chunkSource.getChunkNow(anyInt(), anyInt())).thenReturn(
                fixture.chunk()
        );
        when(fixture.chunk().getBlockState(any(BlockPos.class))).thenReturn(
                Blocks.STONE.defaultBlockState()
        );

        byte[] disabled = serialize(section, fixture, false);
        byte[] enabled = serialize(section, fixture, true);

        assertArrayEquals(disabled, enabled);
        assertSame(
                Blocks.DIAMOND_ORE.defaultBlockState(),
                section.getBlockState(0, 0, 0)
        );
    }

    private static byte[] serialize(
            LevelChunkSection section,
            RuntimeFixture fixture,
            boolean shadowEnabled
    ) {
        if (!shadowEnabled) {
            try (MockedStatic<AntiXrayShadowRuntime> shadowRuntime =
                         mockStatic(AntiXrayShadowRuntime.class, CALLS_REAL_METHODS)) {
                shadowRuntime.when(AntiXrayShadowRuntime::isEnabled)
                        .thenReturn(false);
                return serialize(section, fixture);
            }
        }

        return serialize(section, fixture);
    }

    private static byte[] serialize(
            LevelChunkSection section,
            RuntimeFixture fixture
    ) {
        AntiXrayContext.set(
                fixture.player(),
                fixture.level(),
                fixture.chunk()
        );

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            AntiXrayObfuscator.writeSection(section, buffer);
            return ByteBufUtil.getBytes(buffer);
        } finally {
            buffer.release();
            AntiXrayContext.clear();
        }
    }

    private static RuntimeFixture runtimeFor(LevelChunkSection section) {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        LevelChunk chunk = mock(LevelChunk.class);

        when(chunk.getSections()).thenReturn(
                new LevelChunkSection[]{section}
        );
        when(chunk.getMinY()).thenReturn(0);
        when(chunk.getHeight()).thenReturn(16);
        when(chunk.getPos()).thenReturn(new ChunkPos(0, 0));
        when(chunk.getLevel()).thenReturn(level);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(
                Blocks.STONE.defaultBlockState()
        );

        return new RuntimeFixture(level, player, chunk);
    }

    private static LevelChunkSection sectionWith(BlockState terrain) {
        PalettedContainer<BlockState> states = new PalettedContainer<>(
                Blocks.AIR.defaultBlockState(),
                Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY)
        );

        IdMapper<Holder<Biome>> biomeIds = new IdMapper<>();
        Holder<Biome> biome = Holder.direct(mock(Biome.class));
        biomeIds.add(biome);
        PalettedContainer<Holder<Biome>> biomes = new PalettedContainer<>(
                biome,
                Strategy.createForBiomes(biomeIds)
        );

        LevelChunkSection section = new LevelChunkSection(states, biomes);
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    section.setBlockState(x, y, z, terrain, false);
                }
            }
        }
        section.setBlockState(
                0,
                0,
                0,
                Blocks.DIAMOND_ORE.defaultBlockState(),
                false
        );
        section.recalcBlockCounts();
        return section;
    }

    private record RuntimeFixture(
            ServerLevel level,
            ServerPlayer player,
            LevelChunk chunk
    ) {}
}
