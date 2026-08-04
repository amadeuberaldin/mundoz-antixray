package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.BlockPosition;
import net.minecraft.core.BlockPos;

import java.util.Objects;

public final class BlockPositionMapper {

    private BlockPositionMapper() {}

    public static BlockPosition toDomain(
            BlockPos position
    ) {
        Objects.requireNonNull(
                position,
                "position"
        );

        return new BlockPosition(
                position.getX(),
                position.getY(),
                position.getZ()
        );
    }

    public static BlockPos toMinecraft(
            BlockPosition position
    ) {
        Objects.requireNonNull(
                position,
                "position"
        );

        return new BlockPos(
                position.x(),
                position.y(),
                position.z()
        );
    }
}
