package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockIdentityMapperTest {

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static final Map<Block, BlockIdentity> EXPECTED_MAPPINGS =
            Map.ofEntries(
                    Map.entry(Blocks.COAL_ORE, BlockIdentity.COAL_ORE),
                    Map.entry(Blocks.DEEPSLATE_COAL_ORE, BlockIdentity.DEEPSLATE_COAL_ORE),
                    Map.entry(Blocks.COPPER_ORE, BlockIdentity.COPPER_ORE),
                    Map.entry(Blocks.DEEPSLATE_COPPER_ORE, BlockIdentity.DEEPSLATE_COPPER_ORE),
                    Map.entry(Blocks.IRON_ORE, BlockIdentity.IRON_ORE),
                    Map.entry(Blocks.DEEPSLATE_IRON_ORE, BlockIdentity.DEEPSLATE_IRON_ORE),
                    Map.entry(Blocks.GOLD_ORE, BlockIdentity.GOLD_ORE),
                    Map.entry(Blocks.DEEPSLATE_GOLD_ORE, BlockIdentity.DEEPSLATE_GOLD_ORE),
                    Map.entry(Blocks.REDSTONE_ORE, BlockIdentity.REDSTONE_ORE),
                    Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, BlockIdentity.DEEPSLATE_REDSTONE_ORE),
                    Map.entry(Blocks.EMERALD_ORE, BlockIdentity.EMERALD_ORE),
                    Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, BlockIdentity.DEEPSLATE_EMERALD_ORE),
                    Map.entry(Blocks.LAPIS_ORE, BlockIdentity.LAPIS_ORE),
                    Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, BlockIdentity.DEEPSLATE_LAPIS_ORE),
                    Map.entry(Blocks.DIAMOND_ORE, BlockIdentity.DIAMOND_ORE),
                    Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, BlockIdentity.DEEPSLATE_DIAMOND_ORE),
                    Map.entry(Blocks.NETHER_GOLD_ORE, BlockIdentity.NETHER_GOLD_ORE),
                    Map.entry(Blocks.NETHER_QUARTZ_ORE, BlockIdentity.NETHER_QUARTZ_ORE),
                    Map.entry(Blocks.ANCIENT_DEBRIS, BlockIdentity.ANCIENT_DEBRIS),
                    Map.entry(Blocks.LAVA, BlockIdentity.LAVA)
            );

    @Test
    void mappingsCoverEveryAcceptedIdentityExactlyOnce() {
        assertEquals(
                20,
                EXPECTED_MAPPINGS.size()
        );
        assertEquals(
                EnumSet.allOf(BlockIdentity.class),
                EnumSet.copyOf(EXPECTED_MAPPINGS.values())
        );
    }

    @Test
    void everyAcceptedDefaultBlockStateMapsToItsIdentity() {
        for (Map.Entry<Block, BlockIdentity> mapping : EXPECTED_MAPPINGS.entrySet()) {
            assertEquals(
                    mapping.getValue(),
                    BlockIdentityMapper.map(
                            mapping.getKey().defaultBlockState()
                    ).orElseThrow(),
                    mapping.getValue().name()
            );
        }
    }

    @Test
    void documentedExcludedBlocksAreNotMapped() {
        List<Block> excludedBlocks = List.of(
                Blocks.AMETHYST_BLOCK,
                Blocks.BUDDING_AMETHYST,
                Blocks.AMETHYST_CLUSTER,
                Blocks.LARGE_AMETHYST_BUD,
                Blocks.MEDIUM_AMETHYST_BUD,
                Blocks.SMALL_AMETHYST_BUD,
                Blocks.CALCITE,
                Blocks.SMOOTH_BASALT,
                Blocks.TUFF_SLAB
        );

        assertAllUnmapped(excludedBlocks);
    }

    @Test
    void unrelatedTerrainAndBuildingBlocksAreNotMapped() {
        List<Block> unrelatedBlocks = List.of(
                Blocks.AIR,
                Blocks.STONE,
                Blocks.DEEPSLATE,
                Blocks.NETHERRACK,
                Blocks.TUFF,
                Blocks.OAK_PLANKS,
                Blocks.STONE_BRICKS
        );

        assertAllUnmapped(unrelatedBlocks);
    }

    @Test
    void nullStateIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> BlockIdentityMapper.map(null)
        );
    }

    private static void assertAllUnmapped(
            List<Block> blocks
    ) {
        for (Block block : blocks) {
            assertTrue(
                    BlockIdentityMapper.map(
                            block.defaultBlockState()
                    ).isEmpty(),
                    block.toString()
            );
        }
    }
}
