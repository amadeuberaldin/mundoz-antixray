package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MinecraftObservationTargetSampler {
    static final double TARGET_SAMPLE_INSET = 0.0001D;

    public List<Vec3> samples(Vec3 observationOrigin, BlockPos target) {
        Objects.requireNonNull(observationOrigin, "observationOrigin");
        Objects.requireNonNull(target, "target");

        double minX = target.getX();
        double minY = target.getY();
        double minZ = target.getZ();
        double maxX = minX + 1.0D;
        double maxY = minY + 1.0D;
        double maxZ = minZ + 1.0D;
        double centerX = minX + 0.5D;
        double centerY = minY + 0.5D;
        double centerZ = minZ + 0.5D;

        List<Vec3> samples = new ArrayList<>();
        samples.add(new Vec3(centerX, centerY, centerZ));

        if (observationOrigin.x < minX) {
            samples.add(new Vec3(minX + TARGET_SAMPLE_INSET, centerY, centerZ));
        } else if (observationOrigin.x > maxX) {
            samples.add(new Vec3(maxX - TARGET_SAMPLE_INSET, centerY, centerZ));
        }

        if (observationOrigin.y < minY) {
            samples.add(new Vec3(centerX, minY + TARGET_SAMPLE_INSET, centerZ));
        } else if (observationOrigin.y > maxY) {
            samples.add(new Vec3(centerX, maxY - TARGET_SAMPLE_INSET, centerZ));
        }

        if (observationOrigin.z < minZ) {
            samples.add(new Vec3(centerX, centerY, minZ + TARGET_SAMPLE_INSET));
        } else if (observationOrigin.z > maxZ) {
            samples.add(new Vec3(centerX, centerY, maxZ - TARGET_SAMPLE_INSET));
        }

        return List.copyOf(samples);
    }
}
