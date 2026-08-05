package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplacementContextMapperTest {

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private final ReplacementContextMapper mapper =
            new ReplacementContextMapper();

    @Test
    void mapsOnlyAcceptedTerrainRepresentations() {
        assertEquals(
                List.of(
                        ReplacementRepresentation.TUFF,
                        ReplacementRepresentation.STONE,
                        ReplacementRepresentation.DEEPSLATE,
                        ReplacementRepresentation.NETHERRACK,
                        ReplacementRepresentation.END_STONE
                ),
                mapper.mapAvailableRepresentations(
                        List.of(
                                Blocks.TUFF.defaultBlockState(),
                                Blocks.DIAMOND_ORE.defaultBlockState(),
                                Blocks.STONE.defaultBlockState(),
                                Blocks.DEEPSLATE.defaultBlockState(),
                                Blocks.NETHERRACK.defaultBlockState(),
                                Blocks.END_STONE.defaultBlockState()
                        )
                )
        );
    }

    @Test
    void duplicateTerrainStatesProduceOneRepresentation() {
        assertEquals(
                List.of(ReplacementRepresentation.STONE),
                mapper.mapAvailableRepresentations(
                        List.of(
                                Blocks.STONE.defaultBlockState(),
                                Blocks.STONE.defaultBlockState()
                        )
                )
        );
    }

    @Test
    void resultIsImmutable() {
        List<ReplacementRepresentation> representations =
                mapper.mapAvailableRepresentations(
                        List.of(Blocks.STONE.defaultBlockState())
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> representations.add(ReplacementRepresentation.TUFF)
        );
    }

    @Test
    void nullInputsAreRejected() {
        assertThrows(
                NullPointerException.class,
                () -> mapper.mapAvailableRepresentations(null)
        );
        assertThrows(
                NullPointerException.class,
                () -> mapper.mapAvailableRepresentations(
                        Arrays.asList(
                                Blocks.STONE.defaultBlockState(),
                                (BlockState) null
                        )
                )
        );
    }
}
