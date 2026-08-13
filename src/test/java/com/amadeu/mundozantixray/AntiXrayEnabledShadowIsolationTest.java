package com.amadeu.mundozantixray;

import com.amadeu.mundozantixray.application.runtime.RuntimeDecisionComparison;
import com.amadeu.mundozantixray.infrastructure.minecraft.adapter.MinecraftRuntimeShadowEvaluator;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Test
    void diagnosticsDisabledDoNotParticipateInRealShadowExecution() {
        LevelChunkSection section = sectionWith(Blocks.STONE.defaultBlockState());
        RuntimeFixture fixture = runtimeFor(section);
        Entity camera = mock(Entity.class);
        when(fixture.player().getCamera()).thenReturn(camera);
        when(camera.getEyePosition()).thenReturn(new Vec3(-2.0D, 0.5D, 0.5D));
        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(fixture.level().getChunkSource()).thenReturn(chunkSource);
        when(chunkSource.getChunkNow(anyInt(), anyInt())).thenReturn(fixture.chunk());
        when(fixture.chunk().getBlockState(any(BlockPos.class))).thenReturn(
                Blocks.STONE.defaultBlockState());

        try (MockedStatic<ShadowRuntimeDiagnostics> diagnostics = mockStatic(
                ShadowRuntimeDiagnostics.class, CALLS_REAL_METHODS);
             MockedConstruction<MinecraftRuntimeShadowEvaluator.DiagnosticOutcome> outcomes =
                     mockConstruction(MinecraftRuntimeShadowEvaluator.DiagnosticOutcome.class);
             MockedConstruction<ShadowRuntimeValidationReporter> reporters =
                     mockConstruction(ShadowRuntimeValidationReporter.class)) {
            serialize(section, fixture, true);

            diagnostics.verify(ShadowRuntimeDiagnostics::aggregate, never());
            diagnostics.verify(ShadowRuntimeDiagnostics::nanoTime, never());
            assertTrue(outcomes.constructed().isEmpty());
            assertTrue(reporters.constructed().isEmpty());
        }
    }

    @Test
    void everyDryAndWaterloggedCopperGrateIsObservedAtShadowRuntimeBoundary() {
        LevelChunkSection section = sectionWith(Blocks.STONE.defaultBlockState());
        RuntimeFixture fixture = runtimeFor(section);
        Entity camera = mock(Entity.class);
        when(fixture.player().getCamera()).thenReturn(camera);
        when(camera.getEyePosition()).thenReturn(new Vec3(0.5D, 0.5D, 0.5D));

        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(fixture.level().getChunkSource()).thenReturn(chunkSource);
        when(chunkSource.getChunkNow(anyInt(), anyInt())).thenReturn(fixture.chunk());

        for (Block grate : Blocks.COPPER_GRATE.asList()) {
            for (boolean waterlogged : List.of(false, true)) {
                BlockState grateState = grate.defaultBlockState().setValue(
                        BlockStateProperties.WATERLOGGED,
                        waterlogged
                );
                when(fixture.chunk().getBlockState(any(BlockPos.class))).thenAnswer(
                        invocation -> invocation.<BlockPos>getArgument(0).getX() == 1
                                ? grateState
                                : Blocks.AIR.defaultBlockState()
                );

                AntiXrayShadowRuntime.SectionEvaluation evaluation =
                        AntiXrayShadowRuntime.beginSection(section);

                assertSame(
                        RuntimeDecisionComparison.V1_HIDES_V2_REVEALS,
                        evaluation.compare(
                                fixture.level(),
                                fixture.player(),
                                3,
                                0,
                                0,
                                Blocks.DIAMOND_ORE.defaultBlockState(),
                                true
                        ),
                        grate + " waterlogged=" + waterlogged
                );
            }
        }
    }

    @Test
    void verticalCopperGrateShaftIsObservedAtShadowRuntimeBoundary() {
        LevelChunkSection section = sectionWith(Blocks.STONE.defaultBlockState());
        RuntimeFixture fixture = runtimeFor(section);
        Entity camera = mock(Entity.class);
        when(fixture.player().getCamera()).thenReturn(camera);

        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(fixture.level().getChunkSource()).thenReturn(chunkSource);
        when(chunkSource.getChunkNow(anyInt(), anyInt())).thenReturn(fixture.chunk());

        for (Block grate : Blocks.COPPER_GRATE.asList()) {
            for (boolean waterlogged : List.of(false, true)) {
                BlockState grateState = grate.defaultBlockState().setValue(
                        BlockStateProperties.WATERLOGGED, waterlogged
                );
                when(fixture.chunk().getBlockState(any(BlockPos.class))).thenAnswer(
                        invocation -> verticalShaftState(
                                invocation.getArgument(0), grateState
                        )
                );
                for (Vec3 origin : List.of(
                        new Vec3(0.5D, 3.62D, 0.5D),
                        new Vec3(1.5D, 3.62D, 0.5D),
                        new Vec3(0.5D, 3.62D, 1.5D),
                        new Vec3(1.5D, 3.62D, 1.5D)
                )) {
                    when(camera.getEyePosition()).thenReturn(origin);
                    AntiXrayShadowRuntime.SectionEvaluation evaluation =
                            AntiXrayShadowRuntime.beginSection(section);

                    assertSame(
                            RuntimeDecisionComparison.V1_HIDES_V2_REVEALS,
                            evaluation.compare(
                                    fixture.level(), fixture.player(),
                                    0, 0, 0,
                                    Blocks.DIAMOND_ORE.defaultBlockState(),
                                    true
                            ),
                            grate + " waterlogged=" + waterlogged
                                    + " origin=" + origin
                    );
                }
            }
        }
    }

    private static BlockState verticalShaftState(
            BlockPos position,
            BlockState grateState
    ) {
        if (position.equals(BlockPos.ZERO)) {
            return Blocks.DIAMOND_ORE.defaultBlockState();
        }
        if (position.equals(new BlockPos(0, 1, 0))) {
            return grateState;
        }
        if (position.getY() >= 2) {
            return Blocks.AIR.defaultBlockState();
        }
        return Blocks.END_STONE.defaultBlockState();
    }

    @Test
    void targetedPathDiagnosticUsesAdmittedCandidateWithoutChangingComparison() {
        LevelChunkSection section = sectionWith(Blocks.STONE.defaultBlockState());
        RuntimeFixture fixture = runtimeFor(section);
        Entity camera = mock(Entity.class);
        when(fixture.player().getCamera()).thenReturn(camera);
        when(camera.getEyePosition()).thenReturn(new Vec3(0.5D, 3.62D, 0.5D));
        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(fixture.level().getChunkSource()).thenReturn(chunkSource);
        when(chunkSource.getChunkNow(anyInt(), anyInt())).thenReturn(fixture.chunk());
        when(fixture.chunk().getBlockState(any(BlockPos.class))).thenAnswer(
                invocation -> verticalShaftState(
                        invocation.getArgument(0),
                        Blocks.COPPER_GRATE.asList().getFirst().defaultBlockState()
                )
        );

        try (MockedStatic<ShadowPathDiagnostics> diagnostics =
                     mockStatic(ShadowPathDiagnostics.class)) {
            diagnostics.when(() -> ShadowPathDiagnostics.shouldTrace(
                    fixture.level(), BlockPos.ZERO)).thenReturn(true);
            AntiXrayShadowRuntime.SectionEvaluation evaluation =
                    AntiXrayShadowRuntime.beginSection(section);

            assertSame(RuntimeDecisionComparison.V1_HIDES_V2_REVEALS,
                    evaluation.compare(
                            fixture.level(), fixture.player(), 0, 0, 0,
                            Blocks.DIAMOND_ORE.defaultBlockState(), true
                    ));
            diagnostics.verify(() -> ShadowPathDiagnostics.report(
                    any(), any(), any(), any(), any(),
                    org.mockito.ArgumentMatchers.eq(true), any(), any()
            ), times(1));
        }
    }

    @Test
    void pathDiagnosticFailureCannotChangeNormalShadowComparison() {
        LevelChunkSection section = sectionWith(Blocks.STONE.defaultBlockState());
        RuntimeFixture fixture = runtimeFor(section);
        Entity camera = mock(Entity.class);
        when(fixture.player().getCamera()).thenReturn(camera);
        when(camera.getEyePosition()).thenReturn(new Vec3(0.5D, 3.62D, 0.5D));
        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(fixture.level().getChunkSource()).thenReturn(chunkSource);
        when(chunkSource.getChunkNow(anyInt(), anyInt())).thenReturn(fixture.chunk());
        when(fixture.chunk().getBlockState(any(BlockPos.class))).thenReturn(
                Blocks.AIR.defaultBlockState()
        );

        try (MockedStatic<ShadowPathDiagnostics> diagnostics =
                     mockStatic(ShadowPathDiagnostics.class)) {
            diagnostics.when(() -> ShadowPathDiagnostics.shouldTrace(
                    fixture.level(), BlockPos.ZERO)).thenReturn(true);
            diagnostics.when(() -> ShadowPathDiagnostics.report(
                    any(), any(), any(), any(), any(),
                    org.mockito.ArgumentMatchers.eq(true), any(), any()
            )).thenThrow(new IllegalStateException("diagnostic unavailable"));
            AntiXrayShadowRuntime.SectionEvaluation evaluation =
                    AntiXrayShadowRuntime.beginSection(section);

            assertSame(RuntimeDecisionComparison.V1_HIDES_V2_REVEALS,
                    evaluation.compare(
                            fixture.level(), fixture.player(), 0, 0, 0,
                            Blocks.DIAMOND_ORE.defaultBlockState(), true
                    ));
        }
    }

    static byte[] serialize(
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

    static byte[] serialize(
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

    static RuntimeFixture runtimeFor(LevelChunkSection section) {
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

    static LevelChunkSection sectionWith(BlockState terrain) {
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

    record RuntimeFixture(
            ServerLevel level,
            ServerPlayer player,
            LevelChunk chunk
    ) {}
}
