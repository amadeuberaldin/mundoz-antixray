package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplacementRepresentationMapperTest {

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static final Map<ReplacementRepresentation, Block> EXPECTED_MAPPINGS =
            Map.of(
                    ReplacementRepresentation.STONE, Blocks.STONE,
                    ReplacementRepresentation.DEEPSLATE, Blocks.DEEPSLATE,
                    ReplacementRepresentation.NETHERRACK, Blocks.NETHERRACK,
                    ReplacementRepresentation.END_STONE, Blocks.END_STONE,
                    ReplacementRepresentation.TUFF, Blocks.TUFF
            );

    @Test
    void mappingsCoverEveryReplacementRepresentation() {
        assertEquals(
                EnumSet.allOf(ReplacementRepresentation.class),
                EnumSet.copyOf(EXPECTED_MAPPINGS.keySet())
        );
    }

    @Test
    void everyRepresentationMapsToItsValidDefaultBlockState() {
        for (Map.Entry<ReplacementRepresentation, Block> mapping
                : EXPECTED_MAPPINGS.entrySet()) {
            assertEquals(
                    mapping.getValue().defaultBlockState(),
                    ReplacementRepresentationMapper.map(mapping.getKey()),
                    mapping.getKey().name()
            );
        }
    }

    @Test
    void nullRepresentationIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> ReplacementRepresentationMapper.map(null)
        );
    }
}
