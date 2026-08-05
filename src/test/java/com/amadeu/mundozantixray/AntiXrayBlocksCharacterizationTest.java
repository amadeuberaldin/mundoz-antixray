package com.amadeu.mundozantixray;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntiXrayBlocksCharacterizationTest {

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void activeV1OreLikeSetIncludesLegacyAndV2Targets() {
        List<Block> oreLikeBlocks = List.of(
                Blocks.COAL_ORE,
                Blocks.DEEPSLATE_COAL_ORE,
                Blocks.COPPER_ORE,
                Blocks.DEEPSLATE_COPPER_ORE,
                Blocks.IRON_ORE,
                Blocks.DEEPSLATE_IRON_ORE,
                Blocks.GOLD_ORE,
                Blocks.DEEPSLATE_GOLD_ORE,
                Blocks.REDSTONE_ORE,
                Blocks.DEEPSLATE_REDSTONE_ORE,
                Blocks.EMERALD_ORE,
                Blocks.DEEPSLATE_EMERALD_ORE,
                Blocks.LAPIS_ORE,
                Blocks.DEEPSLATE_LAPIS_ORE,
                Blocks.DIAMOND_ORE,
                Blocks.DEEPSLATE_DIAMOND_ORE,
                Blocks.NETHER_GOLD_ORE,
                Blocks.NETHER_QUARTZ_ORE,
                Blocks.ANCIENT_DEBRIS,
                Blocks.AMETHYST_BLOCK,
                Blocks.BUDDING_AMETHYST,
                Blocks.AMETHYST_CLUSTER,
                Blocks.LARGE_AMETHYST_BUD,
                Blocks.MEDIUM_AMETHYST_BUD,
                Blocks.SMALL_AMETHYST_BUD,
                Blocks.CALCITE,
                Blocks.SMOOTH_BASALT,
                Blocks.LAVA
        );

        for (Block block : oreLikeBlocks) {
            assertTrue(
                    AntiXrayBlocks.isOreLike(block.defaultBlockState()),
                    block.toString()
            );
            assertTrue(
                    AntiXrayBlocks.isHiddenOre(block.defaultBlockState()),
                    block.toString()
            );
        }
    }

    @Test
    void activeV1StructureLikeSetContainsTuffSlab() {
        assertTrue(
                AntiXrayBlocks.isStructureLike(
                        Blocks.TUFF_SLAB.defaultBlockState()
                )
        );
        assertTrue(
                AntiXrayBlocks.isHiddenOre(
                        Blocks.TUFF_SLAB.defaultBlockState()
                )
        );
    }

    @Test
    void activeV1LeavesOrdinaryTerrainAndBuildingBlocksVisible() {
        for (Block block : List.of(
                Blocks.STONE,
                Blocks.DEEPSLATE,
                Blocks.NETHERRACK,
                Blocks.END_STONE,
                Blocks.TUFF,
                Blocks.OAK_PLANKS,
                Blocks.STONE_BRICKS
        )) {
            assertFalse(
                    AntiXrayBlocks.isHiddenOre(block.defaultBlockState()),
                    block.toString()
            );
        }
    }
}
