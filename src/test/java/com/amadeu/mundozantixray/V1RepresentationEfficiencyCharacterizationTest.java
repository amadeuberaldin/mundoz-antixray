package com.amadeu.mundozantixray;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Minecraft 26.2 characterization of the active v1 representation boundary.
 * Compression here is an isolated zlib experiment, not a packet measurement.
 */
class V1RepresentationEfficiencyCharacterizationTest {
    private static final int SECTION_CELLS = 16 * 16 * 16;

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void representativeSectionsHaveStableMeasuredCharacteristics() {
        List<Measurement> measurements = fixtures().stream()
                .map(this::measure)
                .toList();

        assertEquals(List.of(
                expected("homogeneous-control", 0, 0, 0, 1, 1, 0, 0,
                        8, 8, 14, 14),
                expected("overworld-sparse", 8, 8, 0, 2, 1, 4, 4,
                        2059, 2059, 37, 31),
                expected("overworld-multi-ore", 48, 48, 0, 7, 1, 4, 4,
                        2070, 2070, 52, 43),
                expected("dense-synthetic", 2048, 2048, 0, 4, 1, 4, 4,
                        2064, 2064, 50, 36),
                expected("mixed-terrain", 128, 128, 0, 12, 6, 4, 4,
                        2079, 2079, 78, 69),
                expected("nether", 96, 96, 0, 4, 1, 4, 4,
                        2065, 2065, 50, 38),
                expected("end-control", 0, 0, 0, 1, 1, 0, 0,
                        9, 9, 15, 15)
        ), measurements.stream().map(Measurement::stable).toList());

        assertSame(null, measurements.get(0).selectedReplacement());
        for (int index = 1; index <= 4; index++) {
            assertSame(Blocks.STONE.defaultBlockState(),
                    measurements.get(index).selectedReplacement());
        }
        assertSame(Blocks.NETHERRACK.defaultBlockState(),
                measurements.get(5).selectedReplacement());
        assertSame(null, measurements.get(6).selectedReplacement());

        assertTrue(measurements.stream()
                .filter(measurement -> measurement.replacedProtected() > 0)
                .allMatch(measurement -> measurement.transformedCompressedBytes()
                        < measurement.originalCompressedBytes()));
    }

    @Test
    void transformedBytesDecodeToTheMeasuredTemporaryRepresentation() {
        Fixture fixture = fixture("overworld-sparse", Blocks.STONE.defaultBlockState(),
                index -> index < 8 ? Blocks.DIAMOND_ORE.defaultBlockState()
                        : Blocks.STONE.defaultBlockState());
        LevelChunkSection authoritative = fixture.section();
        BlockState[] before = states(authoritative);

        byte[] transformedBytes = serializeThroughV1(fixture);
        LevelChunkSection transformed = decode(transformedBytes, authoritative);

        assertArrayEquals(before, states(authoritative));
        assertNotSame(authoritative, transformed);
        for (int index = 0; index < 8; index++) {
            assertSame(Blocks.STONE.defaultBlockState(), stateAt(transformed, index));
            assertSame(Blocks.DIAMOND_ORE.defaultBlockState(), stateAt(authoritative, index));
        }
    }

    @Test
    void paletteStorageDoesNotShrinkWhenActualStateDiversityFalls() {
        Measurement measurement = measure(fixtures().stream()
                .filter(fixture -> fixture.name().equals("overworld-multi-ore"))
                .findFirst().orElseThrow());

        assertEquals(7, measurement.originalDistinctStates());
        assertEquals(1, measurement.transformedDistinctStates());
        assertEquals(4, measurement.originalBitsPerEntry());
        assertEquals(4, measurement.transformedBitsPerEntry());
        assertEquals(measurement.originalSerializedBytes(),
                measurement.transformedSerializedBytes());
    }

    @Test
    void crossingAnActualStateThresholdDoesNotCompactTheV1CopyPalette() {
        List<BlockState> protectedStates = List.of(
                Blocks.COAL_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_COAL_ORE.defaultBlockState(),
                Blocks.COPPER_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState(),
                Blocks.IRON_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(),
                Blocks.GOLD_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState(),
                Blocks.REDSTONE_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState(),
                Blocks.EMERALD_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState(),
                Blocks.LAPIS_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState(),
                Blocks.DIAMOND_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState()
        );
        Measurement measurement = measure(fixture(
                "palette-threshold", Blocks.STONE.defaultBlockState(),
                index -> index < protectedStates.size()
                        ? protectedStates.get(index)
                        : Blocks.STONE.defaultBlockState()
        ));

        assertEquals(17, measurement.originalDistinctStates());
        assertEquals(1, measurement.transformedDistinctStates());
        assertEquals(5, measurement.originalBitsPerEntry());
        assertEquals(5, measurement.transformedBitsPerEntry());
        assertEquals(measurement.originalSerializedBytes(),
                measurement.transformedSerializedBytes());
    }

    private Measurement measure(Fixture fixture) {
        LevelChunkSection original = fixture.section();
        BlockState[] authoritativeBefore = states(original);
        byte[] originalBytes = serialize(original);
        byte[] transformedBytes = serializeThroughV1(fixture);
        LevelChunkSection transformed = decode(transformedBytes, original);

        assertArrayEquals(authoritativeBefore, states(original), fixture.name());
        assertEquals(original.getSerializedSize(), originalBytes.length, fixture.name());
        assertEquals(transformed.getSerializedSize(), transformedBytes.length, fixture.name());

        int protectedCandidates = 0;
        int replacedProtected = 0;
        int unchangedProtected = 0;
        BlockState replacement = null;
        for (int index = 0; index < SECTION_CELLS; index++) {
            BlockState before = authoritativeBefore[index];
            BlockState after = stateAt(transformed, index);
            if (AntiXrayBlocks.isHiddenOre(before)) {
                protectedCandidates++;
                if (before == after) {
                    unchangedProtected++;
                } else {
                    replacedProtected++;
                    if (replacement == null) {
                        replacement = after;
                    } else {
                        assertSame(replacement, after, fixture.name());
                    }
                }
            } else {
                assertSame(before, after, fixture.name() + " index=" + index);
            }
        }

        return new Measurement(
                fixture.name(), SECTION_CELLS, protectedCandidates,
                replacedProtected, unchangedProtected,
                distinctStates(original), distinctStates(transformed), replacement,
                original.getStates().bitsPerEntry(), transformed.getStates().bitsPerEntry(),
                originalBytes.length, transformedBytes.length,
                compressedSize(originalBytes), compressedSize(transformedBytes)
        );
    }

    private static List<Fixture> fixtures() {
        List<BlockState> ores = List.of(
                Blocks.COAL_ORE.defaultBlockState(),
                Blocks.COPPER_ORE.defaultBlockState(),
                Blocks.IRON_ORE.defaultBlockState(),
                Blocks.GOLD_ORE.defaultBlockState(),
                Blocks.REDSTONE_ORE.defaultBlockState(),
                Blocks.DIAMOND_ORE.defaultBlockState()
        );
        List<BlockState> mixedTerrain = List.of(
                Blocks.STONE.defaultBlockState(),
                Blocks.DEEPSLATE.defaultBlockState(),
                Blocks.TUFF.defaultBlockState(),
                Blocks.GRANITE.defaultBlockState(),
                Blocks.DIORITE.defaultBlockState(),
                Blocks.ANDESITE.defaultBlockState()
        );
        List<BlockState> netherResources = List.of(
                Blocks.NETHER_GOLD_ORE.defaultBlockState(),
                Blocks.NETHER_QUARTZ_ORE.defaultBlockState(),
                Blocks.ANCIENT_DEBRIS.defaultBlockState()
        );

        return List.of(
                fixture("homogeneous-control", Blocks.STONE.defaultBlockState(),
                        index -> Blocks.STONE.defaultBlockState()),
                fixture("overworld-sparse", Blocks.STONE.defaultBlockState(),
                        index -> index < 8 ? Blocks.DIAMOND_ORE.defaultBlockState()
                                : Blocks.STONE.defaultBlockState()),
                fixture("overworld-multi-ore", Blocks.STONE.defaultBlockState(),
                        index -> index < 48 ? ores.get(index % ores.size())
                                : Blocks.STONE.defaultBlockState()),
                fixture("dense-synthetic", Blocks.STONE.defaultBlockState(),
                        index -> index % 2 == 0
                                ? ores.get(index % 3)
                                : Blocks.STONE.defaultBlockState()),
                fixture("mixed-terrain", Blocks.STONE.defaultBlockState(),
                        index -> index < 128 ? ores.get(index % ores.size())
                                : mixedTerrain.get(index % mixedTerrain.size())),
                fixture("nether", Blocks.NETHERRACK.defaultBlockState(),
                        index -> index < 96 ? netherResources.get(index % netherResources.size())
                                : Blocks.NETHERRACK.defaultBlockState()),
                fixture("end-control", Blocks.END_STONE.defaultBlockState(),
                        index -> Blocks.END_STONE.defaultBlockState())
        );
    }

    private static Fixture fixture(
            String name,
            BlockState surroundingState,
            IntFunction<BlockState> stateAt
    ) {
        LevelChunkSection section = emptySection(surroundingState);
        for (int index = 0; index < SECTION_CELLS; index++) {
            int x = index & 15;
            int z = (index >> 4) & 15;
            int y = (index >> 8) & 15;
            section.setBlockState(x, y, z, stateAt.apply(index), false);
        }
        section.recalcBlockCounts();

        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        LevelChunk chunk = mock(LevelChunk.class);
        when(chunk.getSections()).thenReturn(new LevelChunkSection[]{section});
        when(chunk.getMinY()).thenReturn(0);
        when(chunk.getHeight()).thenReturn(16);
        when(chunk.getPos()).thenReturn(new ChunkPos(0, 0));
        when(chunk.getLevel()).thenReturn(level);
        when(level.getBlockState(any())).thenReturn(surroundingState);
        return new Fixture(name, section, level, player, chunk);
    }

    private static LevelChunkSection emptySection(BlockState initialState) {
        PalettedContainer<BlockState> states = new PalettedContainer<>(
                initialState,
                Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY)
        );
        IdMapper<Holder<Biome>> biomeIds = new IdMapper<>();
        Holder<Biome> biome = Holder.direct(mock(Biome.class));
        biomeIds.add(biome);
        PalettedContainer<Holder<Biome>> biomes = new PalettedContainer<>(
                biome, Strategy.createForBiomes(biomeIds)
        );
        return new LevelChunkSection(states, biomes);
    }

    private static byte[] serializeThroughV1(Fixture fixture) {
        AntiXrayContext.set(fixture.player(), fixture.level(), fixture.chunk());
        try {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                AntiXrayObfuscator.writeSection(fixture.section(), buffer);
                return ByteBufUtil.getBytes(buffer);
            } finally {
                buffer.release();
            }
        } finally {
            AntiXrayContext.clear();
        }
    }

    private static byte[] serialize(LevelChunkSection section) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            section.write(buffer);
            return ByteBufUtil.getBytes(buffer);
        } finally {
            buffer.release();
        }
    }

    private static LevelChunkSection decode(byte[] bytes, LevelChunkSection template) {
        LevelChunkSection decoded = template.copy();
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        try {
            decoded.read(buffer);
            assertEquals(0, buffer.readableBytes());
            return decoded;
        } finally {
            buffer.release();
        }
    }

    private static int distinctStates(LevelChunkSection section) {
        return new LinkedHashSet<>(List.of(states(section))).size();
    }

    private static BlockState[] states(LevelChunkSection section) {
        BlockState[] result = new BlockState[SECTION_CELLS];
        for (int index = 0; index < SECTION_CELLS; index++) {
            result[index] = stateAt(section, index);
        }
        return result;
    }

    private static BlockState stateAt(LevelChunkSection section, int index) {
        return section.getBlockState(index & 15, (index >> 8) & 15, (index >> 4) & 15);
    }

    private static int compressedSize(byte[] bytes) {
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        try {
            deflater.setInput(bytes);
            deflater.finish();
            ByteArrayOutputStream output = new ByteArrayOutputStream(bytes.length);
            byte[] buffer = new byte[512];
            while (!deflater.finished()) {
                int written = deflater.deflate(buffer);
                output.write(buffer, 0, written);
            }
            return output.size();
        } finally {
            deflater.end();
        }
    }

    private static StableMeasurement expected(
            String name,
            int protectedCandidates,
            int replacedProtected,
            int unchangedProtected,
            int originalDistinctStates,
            int transformedDistinctStates,
            int originalBitsPerEntry,
            int transformedBitsPerEntry,
            int originalSerializedBytes,
            int transformedSerializedBytes,
            int originalCompressedBytes,
            int transformedCompressedBytes
    ) {
        return new StableMeasurement(name, protectedCandidates, replacedProtected,
                unchangedProtected, originalDistinctStates, transformedDistinctStates,
                originalBitsPerEntry, transformedBitsPerEntry,
                originalSerializedBytes, transformedSerializedBytes,
                originalCompressedBytes, transformedCompressedBytes);
    }

    private record Fixture(
            String name,
            LevelChunkSection section,
            ServerLevel level,
            ServerPlayer player,
            LevelChunk chunk
    ) {}

    private record Measurement(
            String name,
            int totalCells,
            int protectedCandidates,
            int replacedProtected,
            int unchangedProtected,
            int originalDistinctStates,
            int transformedDistinctStates,
            BlockState selectedReplacement,
            int originalBitsPerEntry,
            int transformedBitsPerEntry,
            int originalSerializedBytes,
            int transformedSerializedBytes,
            int originalCompressedBytes,
            int transformedCompressedBytes
    ) {
        StableMeasurement stable() {
            return new StableMeasurement(name, protectedCandidates, replacedProtected,
                    unchangedProtected, originalDistinctStates, transformedDistinctStates,
                    originalBitsPerEntry, transformedBitsPerEntry,
                    originalSerializedBytes, transformedSerializedBytes,
                    originalCompressedBytes, transformedCompressedBytes);
        }
    }

    private record StableMeasurement(
            String name,
            int protectedCandidates,
            int replacedProtected,
            int unchangedProtected,
            int originalDistinctStates,
            int transformedDistinctStates,
            int originalBitsPerEntry,
            int transformedBitsPerEntry,
            int originalSerializedBytes,
            int transformedSerializedBytes,
            int originalCompressedBytes,
            int transformedCompressedBytes
    ) {}
}
