package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftObservationTargetSamplerTest {
    private static final BlockPos TARGET = new BlockPos(10, 20, 30);
    private static final double INSET = MinecraftObservationTargetSampler.TARGET_SAMPLE_INSET;

    private final MinecraftObservationTargetSampler sampler =
            new MinecraftObservationTargetSampler();

    @Test
    void axisAlignedOriginSelectsCenterThenOneFacingFace() {
        assertEquals(
                List.of(
                        new Vec3(10.5D, 20.5D, 30.5D),
                        new Vec3(10.0D + INSET, 20.5D, 30.5D)
                ),
                sampler.samples(new Vec3(5.0D, 20.5D, 30.5D), TARGET)
        );
    }

    @Test
    void twoAxisDiagonalSelectsFacesInAxisOrder() {
        assertEquals(
                List.of(
                        new Vec3(10.5D, 20.5D, 30.5D),
                        new Vec3(10.0D + INSET, 20.5D, 30.5D),
                        new Vec3(10.5D, 21.0D - INSET, 30.5D)
                ),
                sampler.samples(new Vec3(5.0D, 25.0D, 30.5D), TARGET)
        );
    }

    @Test
    void threeAxisDiagonalSelectsAllFacingFacesInAxisOrder() {
        assertEquals(
                List.of(
                        new Vec3(10.5D, 20.5D, 30.5D),
                        new Vec3(11.0D - INSET, 20.5D, 30.5D),
                        new Vec3(10.5D, 20.0D + INSET, 30.5D),
                        new Vec3(10.5D, 20.5D, 31.0D - INSET)
                ),
                sampler.samples(new Vec3(15.0D, 15.0D, 35.0D), TARGET)
        );
    }

    @Test
    void everyAxisDirectionSelectsTheExpectedFace() {
        assertFace(new Vec3(5.0D, 20.5D, 30.5D), new Vec3(10.0D + INSET, 20.5D, 30.5D));
        assertFace(new Vec3(15.0D, 20.5D, 30.5D), new Vec3(11.0D - INSET, 20.5D, 30.5D));
        assertFace(new Vec3(10.5D, 15.0D, 30.5D), new Vec3(10.5D, 20.0D + INSET, 30.5D));
        assertFace(new Vec3(10.5D, 25.0D, 30.5D), new Vec3(10.5D, 21.0D - INSET, 30.5D));
        assertFace(new Vec3(10.5D, 20.5D, 25.0D), new Vec3(10.5D, 20.5D, 30.0D + INSET));
        assertFace(new Vec3(10.5D, 20.5D, 35.0D), new Vec3(10.5D, 20.5D, 31.0D - INSET));
    }

    @Test
    void originOnClosedBoundsOrInsideSelectsOnlyCenter() {
        Vec3 center = new Vec3(10.5D, 20.5D, 30.5D);

        for (Vec3 origin : List.of(
                new Vec3(10.0D, 20.5D, 30.5D),
                new Vec3(11.0D, 20.5D, 30.5D),
                new Vec3(10.5D, 20.0D, 30.5D),
                new Vec3(10.5D, 21.0D, 30.5D),
                new Vec3(10.5D, 20.5D, 30.0D),
                new Vec3(10.5D, 20.5D, 31.0D),
                center
        )) {
            assertEquals(List.of(center), sampler.samples(origin, TARGET));
        }
    }

    @Test
    void everySampleIsInsideTheTargetCell() {
        for (Vec3 sample : sampler.samples(new Vec3(15.0D, 15.0D, 35.0D), TARGET)) {
            assertTrue(sample.x > 10.0D && sample.x < 11.0D);
            assertTrue(sample.y > 20.0D && sample.y < 21.0D);
            assertTrue(sample.z > 30.0D && sample.z < 31.0D);
        }
    }

    @Test
    void nullInputsAreRejected() {
        assertThrows(NullPointerException.class, () -> sampler.samples(null, TARGET));
        assertThrows(NullPointerException.class,
                () -> sampler.samples(Vec3.ZERO, null));
    }

    private void assertFace(Vec3 origin, Vec3 expectedFace) {
        assertEquals(
                List.of(new Vec3(10.5D, 20.5D, 30.5D), expectedFace),
                sampler.samples(origin, TARGET)
        );
    }
}
