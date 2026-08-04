package com.amadeu.mundozantixray.domain.replacement;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplacementContextTest {

    @Test
    void availableRepresentationsAreDefensivelyCopied() {
        List<ReplacementRepresentation> available = new ArrayList<>();
        available.add(ReplacementRepresentation.STONE);

        ReplacementContext context = new ReplacementContext(
                BlockIdentity.DIAMOND_ORE,
                available
        );
        available.add(ReplacementRepresentation.DEEPSLATE);

        assertEquals(
                List.of(ReplacementRepresentation.STONE),
                context.availableRepresentations()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> context.availableRepresentations().add(
                        ReplacementRepresentation.TUFF
                )
        );
    }

    @Test
    void nullInputsAreRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new ReplacementContext(
                        null,
                        List.of()
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> new ReplacementContext(
                        BlockIdentity.DIAMOND_ORE,
                        null
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> new ReplacementContext(
                        BlockIdentity.DIAMOND_ORE,
                        java.util.Arrays.asList(
                                ReplacementRepresentation.STONE,
                                null
                        )
                )
        );
    }
}
