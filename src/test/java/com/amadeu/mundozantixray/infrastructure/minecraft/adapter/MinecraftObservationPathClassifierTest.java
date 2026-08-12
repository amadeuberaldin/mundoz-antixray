package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinecraftObservationPathClassifierTest {
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private final MinecraftObservationPathClassifier classifier =
            new MinecraftObservationPathClassifier();

    @Test
    void approvedPassThroughCategoriesDoNotTerminateObservation() {
        List<Block> passThroughBlocks = List.of(
                Blocks.AIR,
                Blocks.CAVE_AIR,
                Blocks.WATER,
                Blocks.GLASS,
                Blocks.STAINED_GLASS.white(),
                Blocks.TINTED_GLASS,
                Blocks.GLASS_PANE,
                Blocks.STAINED_GLASS_PANE.white(),
                Blocks.CRAFTING_TABLE,
                Blocks.BREWING_STAND,
                Blocks.OAK_DOOR,
                Blocks.IRON_DOOR,
                Blocks.OAK_TRAPDOOR,
                Blocks.IRON_TRAPDOOR,
                Blocks.OAK_FENCE,
                Blocks.NETHER_BRICK_FENCE,
                Blocks.OAK_FENCE_GATE,
                Blocks.IRON_BARS,
                Blocks.IRON_CHAIN,
                Blocks.LANTERN,
                Blocks.SOUL_LANTERN,
                Blocks.STONE_BUTTON,
                Blocks.OAK_BUTTON,
                Blocks.LEVER,
                Blocks.COPPER_GRATE.asList().getFirst(),
                Blocks.OAK_STAIRS,
                Blocks.DEEPSLATE_BRICK_STAIRS
        );

        for (Block block : passThroughBlocks) {
            assertEquals(
                    ObservationPathBehavior.PASS_THROUGH,
                    classifier.classify(block.defaultBlockState()),
                    block.toString()
            );
        }
    }

    @Test
    void lavaAndOrdinaryVisualWallsTerminateObservation() {
        for (Block block : List.of(
                Blocks.LAVA,
                Blocks.STONE,
                Blocks.DEEPSLATE,
                Blocks.DIRT,
                Blocks.GRANITE,
                Blocks.ANDESITE,
                Blocks.SMOOTH_BASALT,
                Blocks.NETHERRACK,
                Blocks.END_STONE,
                Blocks.DIAMOND_ORE,
                Blocks.OAK_PLANKS,
                Blocks.STONE_BRICKS
        )) {
            assertEquals(
                    ObservationPathBehavior.OCCLUDING,
                    classifier.classify(block.defaultBlockState()),
                    block.toString()
            );
        }
    }

    @Test
    void nonSolidIsNotAUniversalPassThroughRule() {
        assertEquals(
                ObservationPathBehavior.OCCLUDING,
                classifier.classify(Blocks.LAVA.defaultBlockState())
        );
    }

    @Test
    void nullStateIsRejected() {
        assertThrows(NullPointerException.class, () -> classifier.classify(null));
    }
}
