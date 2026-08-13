package com.amadeu.mundozantixray;

import io.netty.buffer.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.network.FriendlyByteBuf;
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

/** Structural counts at the client-installed LevelChunkSection boundary. */
class ClientRepresentationStructureCharacterizationTest {
    static { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test void v1ReducesInstalledStateDiversityButNotCompilerBranchCounts() {
        List<Comparison> results=fixtures().stream().map(this::compare).toList();
        assertEquals(List.of(
            stable("terrain-control",1,1,0,0,4096,4096,4096,4096,0,0,0,0),
            stable("sparse",2,1,4,4,4096,4096,4096,4096,0,0,0,0),
            stable("resource-diverse",7,1,4,4,4096,4096,4096,4096,0,0,0,0),
            stable("unrelated-diverse-control",16,16,4,4,4096,4096,4096,4096,0,0,0,0)
        ),results.stream().map(Comparison::stable).toList());
        results.forEach(r->assertArrayEquals(r.authoritativeBefore(),states(r.fixture().section())));
    }

    @Test void oreAndTerrainStatesTakeTheSameSectionCompilerStructuralBranches() {
        for(BlockState state:List.of(Blocks.STONE.defaultBlockState(),Blocks.DIAMOND_ORE.defaultBlockState(),Blocks.COAL_ORE.defaultBlockState(),Blocks.COPPER_ORE.defaultBlockState(),Blocks.IRON_ORE.defaultBlockState(),Blocks.GOLD_ORE.defaultBlockState(),Blocks.REDSTONE_ORE.defaultBlockState())) {
            assertFalse(state.isAir(),state.toString());
            assertTrue(state.isSolidRender(),state.toString());
            assertEquals(RenderShape.MODEL,state.getRenderShape(),state.toString());
            assertTrue(state.getFluidState().isEmpty(),state.toString());
            assertFalse(state.hasBlockEntity(),state.toString());
        }
    }

    @Test void repeatedSectionsScaleStructuralWorkEqually() {
        Comparison sparse=compare(fixtures().get(1));
        for(int sections:List.of(1,4,16,24)) {
            assertEquals(4096*sections,sparse.original().visited()*sections);
            assertEquals(sparse.original().modelStates()*sections,sparse.v1().modelStates()*sections);
            assertEquals(sparse.original().solidStates()*sections,sparse.v1().solidStates()*sections);
        }
    }

    private Comparison compare(Fixture fixture) {
        BlockState[] before=states(fixture.section());
        LevelChunkSection original=decode(serialize(fixture.section()),fixture.section());
        LevelChunkSection v1=decode(serializeV1(fixture),fixture.section());
        return new Comparison(fixture,before,metrics(original),metrics(v1));
    }

    private static Metrics metrics(LevelChunkSection section) {
        Set<BlockState> distinct=new HashSet<>(); int air=0,solid=0,model=0,fluid=0,entities=0;
        for(BlockState state:states(section)){distinct.add(state);if(state.isAir())air++;if(state.isSolidRender())solid++;if(state.getRenderShape()==RenderShape.MODEL)model++;if(!state.getFluidState().isEmpty())fluid++;if(state.hasBlockEntity())entities++;}
        return new Metrics(4096,distinct.size(),section.getStates().bitsPerEntry(),air,solid,model,fluid,entities);
    }

    private static List<Fixture> fixtures(){
        List<BlockState> ores=List.of(Blocks.COAL_ORE.defaultBlockState(),Blocks.COPPER_ORE.defaultBlockState(),Blocks.IRON_ORE.defaultBlockState(),Blocks.GOLD_ORE.defaultBlockState(),Blocks.REDSTONE_ORE.defaultBlockState(),Blocks.DIAMOND_ORE.defaultBlockState());
        List<BlockState> unrelated=List.of(Blocks.STONE.defaultBlockState(),Blocks.DEEPSLATE.defaultBlockState(),Blocks.TUFF.defaultBlockState(),Blocks.GRANITE.defaultBlockState(),Blocks.DIORITE.defaultBlockState(),Blocks.ANDESITE.defaultBlockState(),Blocks.DIRT.defaultBlockState(),Blocks.COBBLESTONE.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState(),Blocks.OAK_PLANKS.defaultBlockState(),Blocks.BRICKS.defaultBlockState(),Blocks.MUD_BRICKS.defaultBlockState(),Blocks.NETHERRACK.defaultBlockState(),Blocks.END_STONE.defaultBlockState(),Blocks.BASALT.defaultBlockState(),Blocks.BLACKSTONE.defaultBlockState());
        return List.of(
          fixture("terrain-control",Blocks.STONE.defaultBlockState(),i->Blocks.STONE.defaultBlockState()),
          fixture("sparse",Blocks.STONE.defaultBlockState(),i->i<8?Blocks.DIAMOND_ORE.defaultBlockState():Blocks.STONE.defaultBlockState()),
          fixture("resource-diverse",Blocks.STONE.defaultBlockState(),i->i<48?ores.get(i%ores.size()):Blocks.STONE.defaultBlockState()),
          fixture("unrelated-diverse-control",Blocks.STONE.defaultBlockState(),i->unrelated.get(i%unrelated.size())));
    }

    private static Fixture fixture(String name,BlockState surrounding,IntFunction<BlockState> fn){
        LevelChunkSection s=empty(surrounding);for(int i=0;i<4096;i++)s.setBlockState(i&15,(i>>8)&15,(i>>4)&15,fn.apply(i),false);s.recalcBlockCounts();
        ServerLevel level=mock(ServerLevel.class);ServerPlayer player=mock(ServerPlayer.class);LevelChunk chunk=mock(LevelChunk.class);when(chunk.getSections()).thenReturn(new LevelChunkSection[]{s});when(chunk.getMinY()).thenReturn(0);when(chunk.getHeight()).thenReturn(16);when(chunk.getPos()).thenReturn(new ChunkPos(0,0));when(chunk.getLevel()).thenReturn(level);when(level.getBlockState(any())).thenReturn(surrounding);return new Fixture(name,s,level,player,chunk);
    }
    private static LevelChunkSection empty(BlockState initial){PalettedContainer<BlockState>s=new PalettedContainer<>(initial,Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY));IdMapper<Holder<Biome>>ids=new IdMapper<>();Holder<Biome>b=Holder.direct(mock(Biome.class));ids.add(b);return new LevelChunkSection(s,new PalettedContainer<>(b,Strategy.createForBiomes(ids)));}
    private static byte[] serialize(LevelChunkSection s){FriendlyByteBuf b=new FriendlyByteBuf(Unpooled.buffer());try{s.write(b);return ByteBufUtil.getBytes(b);}finally{b.release();}}
    private static byte[] serializeV1(Fixture f){AntiXrayContext.set(f.player(),f.level(),f.chunk());try{FriendlyByteBuf b=new FriendlyByteBuf(Unpooled.buffer());try{AntiXrayObfuscator.writeSection(f.section(),b);return ByteBufUtil.getBytes(b);}finally{b.release();}}finally{AntiXrayContext.clear();}}
    private static LevelChunkSection decode(byte[]bytes,LevelChunkSection template){LevelChunkSection result=template.copy();FriendlyByteBuf b=new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));try{result.read(b);assertEquals(0,b.readableBytes());return result;}finally{b.release();}}
    private static BlockState[] states(LevelChunkSection s){BlockState[]r=new BlockState[4096];for(int i=0;i<4096;i++)r[i]=s.getBlockState(i&15,(i>>8)&15,(i>>4)&15);return r;}
    private static Stable stable(String n,int od,int vd,int ob,int vb,int os,int vs,int om,int vm,int oa,int va,int of,int vf){return new Stable(n,od,vd,ob,vb,os,vs,om,vm,oa,va,of,vf);}
    private record Fixture(String name,LevelChunkSection section,ServerLevel level,ServerPlayer player,LevelChunk chunk){}
    private record Metrics(int visited,int distinct,int bits,int air,int solidStates,int modelStates,int fluidStates,int blockEntities){}
    private record Comparison(Fixture fixture,BlockState[]authoritativeBefore,Metrics original,Metrics v1){Stable stable(){return new Stable(fixture.name(),original.distinct(),v1.distinct(),original.bits(),v1.bits(),original.solidStates(),v1.solidStates(),original.modelStates(),v1.modelStates(),original.air(),v1.air(),original.fluidStates(),v1.fluidStates());}}
    private record Stable(String name,int originalDistinct,int v1Distinct,int originalBits,int v1Bits,int originalSolid,int v1Solid,int originalModel,int v1Model,int originalAir,int v1Air,int originalFluid,int v1Fluid){}
}
