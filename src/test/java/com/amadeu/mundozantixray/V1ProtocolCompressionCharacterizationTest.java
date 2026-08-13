package com.amadeu.mundozantixray;

import io.netty.buffer.*;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.network.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.function.IntFunction;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class V1ProtocolCompressionCharacterizationTest {
    private static final int THRESHOLD = 256;
    static { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test void singleSectionProtocolUnitsHaveStableMeasurements() {
        List<Result> results = fixtures().stream().map(f -> measure(f, 1, THRESHOLD)).toList();
        results.forEach(System.out::println);
        results.forEach(r -> assertEquals(r.originalSection(), r.v1Section()));
        assertFalse(results.get(0).originalEligible()); assertFalse(results.get(6).originalEligible());
        results.subList(1, 6).forEach(r -> assertTrue(r.originalEligible() && r.v1Eligible()));
        assertEquals(List.of(28,43,60,58,86,57,29), results.stream().map(Result::originalFinal).toList());
        assertEquals(List.of(28,39,50,44,77,45,29), results.stream().map(Result::v1Final).toList());
        assertEquals(results.get(0).originalFinal(), results.get(0).v1Final());
        assertEquals(results.get(6).originalFinal(), results.get(6).v1Final());
        assertTrue(results.subList(1, 6).stream().allMatch(r -> r.v1Final() < r.originalFinal()));
    }

    @Test void multipleSectionsExposePacketLevelRepetition() {
        Fixture sparse = fixtures().get(1);
        for (int count : List.of(1, 4, 16, 24)) {
            Result result = measure(sparse, count, THRESHOLD);
            System.out.println(result);
            assertTrue(result.v1Final() < result.originalFinal());
        }
        Result mostlyUnchanged = mixedPacket(fixtures().getFirst(), sparse, 15, 1);
        System.out.println(mostlyUnchanged);
        assertTrue(mostlyUnchanged.v1Final() < mostlyUnchanged.originalFinal());
    }

    @Test void thresholdUsesTheCompletePacketPayload() {
        for (int size : List.of(255, 256, 257)) {
            Unit unit = protocolUnit(new byte[size], THRESHOLD);
            assertEquals(size >= THRESHOLD, unit.eligible());
            assertEquals(size >= THRESHOLD ? size : 0, unit.marker());
            assertEquals(unit.compressionBytes() + varIntSize(unit.compressionBytes()), unit.finalBytes());
            if (size < THRESHOLD) assertEquals(size + 1, unit.compressionBytes());
            else assertTrue(unit.compressionBytes() < size);
        }
    }

    private Result measure(Fixture f, int count, int threshold) {
        byte[] original = serialize(f.section());
        byte[] v1 = serializeV1(f);
        byte[] originalPayload = payload(repeat(original, count));
        byte[] v1Payload = payload(repeat(v1, count));
        return result(f.name()+"-"+count, original, v1, originalPayload, v1Payload, threshold);
    }

    private Result mixedPacket(Fixture control, Fixture changed, int controls, int changes) {
        byte[] originalChanged = serialize(changed.section());
        byte[] v1Changed = serializeV1(changed);
        byte[] common = serialize(control.section());
        return result("mostly-unchanged", originalChanged, v1Changed,
                payload(join(common, controls, originalChanged, changes)),
                payload(join(common, controls, v1Changed, changes)), THRESHOLD);
    }

    private static Result result(String name, byte[] os, byte[] vs, byte[] op, byte[] vp, int threshold) {
        Unit o = protocolUnit(op, threshold), v = protocolUnit(vp, threshold);
        return new Result(name, os.length, vs.length, op.length, vp.length, o.eligible(), v.eligible(),
                o.compressionBytes(), v.compressionBytes(), o.frameBytes(), v.frameBytes(), o.finalBytes(), v.finalBytes());
    }

    private static byte[] payload(byte[] sections) {
        FriendlyByteBuf b = new FriendlyByteBuf(Unpooled.buffer());
        try {
            b.writeVarInt(0); b.writeInt(0); b.writeInt(0); b.writeVarInt(0);
            b.writeVarInt(sections.length); b.writeBytes(sections); b.writeVarInt(0);
            for (int i=0;i<4;i++) b.writeBitSet(new BitSet());
            b.writeVarInt(0); b.writeVarInt(0);
            return ByteBufUtil.getBytes(b);
        } finally { b.release(); }
    }

    private static Unit protocolUnit(byte[] payload, int threshold) {
        byte[] compressed = encode(new CompressionEncoder(threshold), payload);
        byte[] framed = encode(new Varint21LengthFieldPrepender(), compressed);
        FriendlyByteBuf b = new FriendlyByteBuf(Unpooled.wrappedBuffer(compressed));
        int marker; try { marker=b.readVarInt(); } finally { b.release(); }
        return new Unit(payload.length>=threshold, marker, compressed.length, framed.length-compressed.length, framed.length);
    }

    private static byte[] encode(io.netty.channel.ChannelOutboundHandler h, byte[] bytes) {
        EmbeddedChannel channel = new EmbeddedChannel(h);
        ByteBuf input = Unpooled.wrappedBuffer(bytes);
        try {
            assertTrue(channel.writeOutbound(input.retain()));
            ByteBuf output = channel.readOutbound();
            try { return ByteBufUtil.getBytes(output); } finally { output.release(); }
        } finally { input.release(); channel.finishAndReleaseAll(); }
    }

    private static byte[] repeat(byte[] bytes, int count) { return join(bytes,count,new byte[0],0); }
    private static byte[] join(byte[] a,int ac,byte[] b,int bc) {
        ByteBuf out=Unpooled.buffer();
        try { for(int i=0;i<ac;i++)out.writeBytes(a); for(int i=0;i<bc;i++)out.writeBytes(b); return ByteBufUtil.getBytes(out); }
        finally { out.release(); }
    }
    private static int varIntSize(int v) { int n=1; while((v&-128)!=0){n++;v>>>=7;} return n; }

    private static List<Fixture> fixtures() {
        List<BlockState> ores=List.of(Blocks.COAL_ORE.defaultBlockState(),Blocks.COPPER_ORE.defaultBlockState(),Blocks.IRON_ORE.defaultBlockState(),Blocks.GOLD_ORE.defaultBlockState(),Blocks.REDSTONE_ORE.defaultBlockState(),Blocks.DIAMOND_ORE.defaultBlockState());
        List<BlockState> mixed=List.of(Blocks.STONE.defaultBlockState(),Blocks.DEEPSLATE.defaultBlockState(),Blocks.TUFF.defaultBlockState(),Blocks.GRANITE.defaultBlockState(),Blocks.DIORITE.defaultBlockState(),Blocks.ANDESITE.defaultBlockState());
        List<BlockState> nether=List.of(Blocks.NETHER_GOLD_ORE.defaultBlockState(),Blocks.NETHER_QUARTZ_ORE.defaultBlockState(),Blocks.ANCIENT_DEBRIS.defaultBlockState());
        return List.of(
            fixture("stone-control",Blocks.STONE.defaultBlockState(),i->Blocks.STONE.defaultBlockState()),
            fixture("overworld-sparse",Blocks.STONE.defaultBlockState(),i->i<8?Blocks.DIAMOND_ORE.defaultBlockState():Blocks.STONE.defaultBlockState()),
            fixture("overworld-multi",Blocks.STONE.defaultBlockState(),i->i<48?ores.get(i%ores.size()):Blocks.STONE.defaultBlockState()),
            fixture("dense-synthetic",Blocks.STONE.defaultBlockState(),i->i%2==0?ores.get(i%3):Blocks.STONE.defaultBlockState()),
            fixture("mixed-terrain",Blocks.STONE.defaultBlockState(),i->i<128?ores.get(i%ores.size()):mixed.get(i%mixed.size())),
            fixture("nether",Blocks.NETHERRACK.defaultBlockState(),i->i<96?nether.get(i%nether.size()):Blocks.NETHERRACK.defaultBlockState()),
            fixture("end-control",Blocks.END_STONE.defaultBlockState(),i->Blocks.END_STONE.defaultBlockState()));
    }

    private static Fixture fixture(String name,BlockState surrounding,IntFunction<BlockState> fn) {
        LevelChunkSection s=empty(surrounding);
        for(int i=0;i<4096;i++)s.setBlockState(i&15,(i>>8)&15,(i>>4)&15,fn.apply(i),false);
        s.recalcBlockCounts();
        ServerLevel level=mock(ServerLevel.class); ServerPlayer player=mock(ServerPlayer.class); LevelChunk chunk=mock(LevelChunk.class);
        when(chunk.getSections()).thenReturn(new LevelChunkSection[]{s}); when(chunk.getMinY()).thenReturn(0); when(chunk.getHeight()).thenReturn(16); when(chunk.getPos()).thenReturn(new ChunkPos(0,0)); when(chunk.getLevel()).thenReturn(level); when(level.getBlockState(any())).thenReturn(surrounding);
        return new Fixture(name,s,level,player,chunk);
    }
    private static LevelChunkSection empty(BlockState initial) {
        PalettedContainer<BlockState> states=new PalettedContainer<>(initial,Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY)); IdMapper<Holder<Biome>> ids=new IdMapper<>(); Holder<Biome> biome=Holder.direct(mock(Biome.class)); ids.add(biome); return new LevelChunkSection(states,new PalettedContainer<>(biome,Strategy.createForBiomes(ids)));
    }
    private static byte[] serialize(LevelChunkSection s) { FriendlyByteBuf b=new FriendlyByteBuf(Unpooled.buffer()); try{s.write(b);return ByteBufUtil.getBytes(b);}finally{b.release();} }
    private static byte[] serializeV1(Fixture f) { AntiXrayContext.set(f.player(),f.level(),f.chunk()); try{FriendlyByteBuf b=new FriendlyByteBuf(Unpooled.buffer());try{AntiXrayObfuscator.writeSection(f.section(),b);return ByteBufUtil.getBytes(b);}finally{b.release();}}finally{AntiXrayContext.clear();} }

    private record Fixture(String name,LevelChunkSection section,ServerLevel level,ServerPlayer player,LevelChunk chunk){}
    private record Unit(boolean eligible,int marker,int compressionBytes,int frameBytes,int finalBytes){}
    private record Result(String name,int originalSection,int v1Section,int originalPayload,int v1Payload,boolean originalEligible,boolean v1Eligible,int originalCompression,int v1Compression,int originalFrame,int v1Frame,int originalFinal,int v1Final){}
}

