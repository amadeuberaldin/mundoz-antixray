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
        assertPassThrough(List.of(
                Blocks.AIR,
                Blocks.CAVE_AIR,
                Blocks.VOID_AIR,
                Blocks.WATER,
                Blocks.GLASS,
                Blocks.TINTED_GLASS,
                Blocks.GLASS_PANE,
                Blocks.CRAFTING_TABLE,
                Blocks.BREWING_STAND,
                Blocks.OAK_DOOR,
                Blocks.OAK_TRAPDOOR,
                Blocks.OAK_FENCE,
                Blocks.NETHER_BRICK_FENCE,
                Blocks.OAK_FENCE_GATE,
                Blocks.IRON_CHAIN,
                Blocks.LANTERN,
                Blocks.SOUL_LANTERN,
                Blocks.STONE_BUTTON,
                Blocks.OAK_BUTTON,
                Blocks.LEVER,
                Blocks.OAK_STAIRS,
                Blocks.DEEPSLATE_BRICK_STAIRS
        ));
    }

    @Test
    void everyStainedGlassColorIsPassThrough() {
        assertPassThrough(Blocks.STAINED_GLASS.asList());
    }

    @Test
    void everyStainedGlassPaneColorIsPassThrough() {
        assertPassThrough(Blocks.STAINED_GLASS_PANE.asList());
    }

    @Test
    void everyWaxedAndWeatheringCopperGrateIsPassThrough() {
        assertPassThrough(Blocks.COPPER_GRATE.asList());
    }

    @Test
    void ironBarsAndEveryCopperBarsVariantArePassThrough() {
        assertPassThrough(List.of(Blocks.IRON_BARS));
        assertPassThrough(Blocks.COPPER_BARS.asList());
    }

    @Test
    void copperFamiliesWithDifferentImplementationClassesRemainPassThrough() {
        assertPassThrough(Blocks.COPPER_CHAIN.asList());
        assertPassThrough(Blocks.COPPER_DOOR.asList());
        assertPassThrough(Blocks.COPPER_TRAPDOOR.asList());
        assertPassThrough(Blocks.COPPER_LANTERN.asList());
        assertPassThrough(Blocks.CUT_COPPER_STAIRS.asList());
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
    void unsupportedPartialBlocksRemainOccluding() {
        assertEquals(
                ObservationPathBehavior.OCCLUDING,
                classifier.classify(Blocks.OAK_SLAB.defaultBlockState())
        );
    }

    @Test
    void nullStateIsRejected() {
        assertThrows(NullPointerException.class, () -> classifier.classify(null));
    }

    private void assertPassThrough(List<Block> blocks) {
        for (Block block : blocks) {
            assertEquals(ObservationPathBehavior.PASS_THROUGH,
                    classifier.classify(block.defaultBlockState()), block.toString());
        }
    }
}
