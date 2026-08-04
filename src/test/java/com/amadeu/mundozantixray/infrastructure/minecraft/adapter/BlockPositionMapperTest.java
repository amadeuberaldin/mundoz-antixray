package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.BlockPosition;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockPositionMapperTest {

    @Test
    void minecraftPositionMapsToDomainPosition() {
        assertEquals(
                new BlockPosition(-12, 64, 37),
                BlockPositionMapper.toDomain(
                        new BlockPos(-12, 64, 37)
                )
        );
    }

    @Test
    void domainPositionMapsToMinecraftPosition() {
        assertEquals(
                new BlockPos(-12, 64, 37),
                BlockPositionMapper.toMinecraft(
                        new BlockPosition(-12, 64, 37)
                )
        );
    }

    @Test
    void nullPositionsAreRejected() {
        assertThrows(
                NullPointerException.class,
                () -> BlockPositionMapper.toDomain(null)
        );
        assertThrows(
                NullPointerException.class,
                () -> BlockPositionMapper.toMinecraft(null)
        );
    }
}
