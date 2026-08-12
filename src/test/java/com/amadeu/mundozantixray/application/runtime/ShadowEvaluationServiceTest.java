package com.amadeu.mundozantixray.application.runtime;

import com.amadeu.mundozantixray.application.observation.ObservationPathEvaluationService;
import com.amadeu.mundozantixray.application.protection.ProtectionEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementIntegrationService;
import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.BlockPosition;
import com.amadeu.mundozantixray.domain.model.ObservationContext;
import com.amadeu.mundozantixray.domain.model.ObservationPathBehavior;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import com.amadeu.mundozantixray.domain.policy.DefaultObservationPolicy;
import com.amadeu.mundozantixray.domain.protection.DefaultBlockProtectionRule;
import com.amadeu.mundozantixray.domain.protection.DefaultProtectionPolicy;
import com.amadeu.mundozantixray.domain.replacement.DefaultReplacementPolicy;
import com.amadeu.mundozantixray.domain.service.ObservationEvaluationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowEvaluationServiceTest {
    private final ShadowEvaluationService service = new ShadowEvaluationService(
            new ObservationPathEvaluationService(
                    new ObservationEvaluationService(
                            new DefaultObservationPolicy()
                    )
            ),
            new ReplacementIntegrationService(
                    new ProtectionEvaluationService(
                            new DefaultProtectionPolicy(
                                    new DefaultBlockProtectionRule()
                            )
                    ),
                    new ReplacementEvaluationService(
                            new DefaultReplacementPolicy()
                    )
            )
    );

    @Test
    void hidesUnobservedProtectedBlockWithSafeReplacement() {
        assertTrue(service.shouldHide(
                BlockIdentity.DIAMOND_ORE,
                contexts(ObservationPathBehavior.OCCLUDING),
                List.of(ReplacementRepresentation.STONE)
        ));
    }

    @Test
    void keepsObservedProtectedBlockVisible() {
        assertFalse(service.shouldHide(
                BlockIdentity.DIAMOND_ORE,
                contexts(ObservationPathBehavior.PASS_THROUGH),
                List.of(ReplacementRepresentation.STONE)
        ));
    }

    @Test
    void unknownPathInformationFailsVisible() {
        assertFalse(service.shouldHide(
                BlockIdentity.DIAMOND_ORE,
                contexts(ObservationPathBehavior.UNKNOWN),
                List.of(ReplacementRepresentation.STONE)
        ));
    }

    @Test
    void keepsUnobservedBlockVisibleWithoutSafeReplacement() {
        assertFalse(service.shouldHide(
                BlockIdentity.ANCIENT_DEBRIS,
                contexts(ObservationPathBehavior.OCCLUDING),
                List.of()
        ));
    }

    @Test
    void rejectsIncompleteInputs() {
        assertThrows(
                NullPointerException.class,
                () -> service.shouldHide(null, contexts(), List.of())
        );
        assertThrows(
                NullPointerException.class,
                () -> service.shouldHide(BlockIdentity.DIAMOND_ORE, null, List.of())
        );
        assertThrows(
                NullPointerException.class,
                () -> service.shouldHide(BlockIdentity.DIAMOND_ORE, contexts(), null)
        );
    }

    private static List<ObservationContext> contexts(
            ObservationPathBehavior... path
    ) {
        return List.of(new ObservationContext(
                new BlockPosition(0, 0, 0),
                new BlockPosition(2, 0, 0),
                List.of(path)
        ));
    }
}
