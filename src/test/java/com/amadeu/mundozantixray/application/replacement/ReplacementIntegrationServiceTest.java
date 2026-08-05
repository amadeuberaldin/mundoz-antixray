package com.amadeu.mundozantixray.application.replacement;

import com.amadeu.mundozantixray.application.protection.ProtectionEvaluationService;
import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;
import com.amadeu.mundozantixray.domain.model.ReplacementDecision;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import com.amadeu.mundozantixray.domain.model.ReplacementResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplacementIntegrationServiceTest {

    @Test
    void unprotectedBlocksRemainVisibleWithoutReplacementEvaluation() {
        AtomicBoolean replacementEvaluated = new AtomicBoolean();
        ReplacementIntegrationService service = service(
                ProtectionDecision.NOT_PROTECTED,
                context -> {
                    replacementEvaluated.set(true);
                    return ReplacementResult.replaceWith(
                            ReplacementRepresentation.STONE
                    );
                }
        );

        ReplacementResult result = service.evaluate(
                BlockIdentity.DIAMOND_ORE,
                List.of(ReplacementRepresentation.STONE)
        );

        assertEquals(ReplacementDecision.KEEP_VISIBLE, result.decision());
        assertFalse(replacementEvaluated.get());
    }

    @Test
    void protectedBlocksReceiveAvailableReplacement() {
        ReplacementIntegrationService service = service(
                ProtectionDecision.PROTECTED,
                context -> ReplacementResult.replaceWith(
                        context.availableRepresentations().getFirst()
                )
        );

        ReplacementResult result = service.evaluate(
                BlockIdentity.DIAMOND_ORE,
                List.of(ReplacementRepresentation.DEEPSLATE)
        );

        assertEquals(ReplacementDecision.REPLACE, result.decision());
        assertEquals(
                ReplacementRepresentation.DEEPSLATE,
                result.representation().orElseThrow()
        );
    }

    @Test
    void protectedBlocksRemainVisibleWithoutSafeReplacement() {
        ReplacementIntegrationService service = service(
                ProtectionDecision.PROTECTED,
                context -> ReplacementResult.keepVisible()
        );

        assertEquals(
                ReplacementResult.keepVisible(),
                service.evaluate(BlockIdentity.LAVA, List.of())
        );
    }

    @Test
    void nullDependenciesAreRejected() {
        ProtectionEvaluationService protectionService =
                new ProtectionEvaluationService(
                        block -> ProtectionDecision.PROTECTED
                );
        ReplacementEvaluationService replacementService =
                new ReplacementEvaluationService(
                        context -> ReplacementResult.keepVisible()
                );

        assertThrows(
                NullPointerException.class,
                () -> new ReplacementIntegrationService(
                        null,
                        replacementService
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> new ReplacementIntegrationService(
                        protectionService,
                        null
                )
        );
    }

    @Test
    void nullInputsAreRejected() {
        ReplacementIntegrationService service = service(
                ProtectionDecision.PROTECTED,
                context -> ReplacementResult.keepVisible()
        );

        assertThrows(
                NullPointerException.class,
                () -> service.evaluate(null, List.of())
        );
        assertThrows(
                NullPointerException.class,
                () -> service.evaluate(BlockIdentity.LAVA, null)
        );
    }

    private static ReplacementIntegrationService service(
            ProtectionDecision protectionDecision,
            com.amadeu.mundozantixray.domain.replacement.ReplacementPolicy replacementPolicy
    ) {
        return new ReplacementIntegrationService(
                new ProtectionEvaluationService(
                        block -> protectionDecision
                ),
                new ReplacementEvaluationService(replacementPolicy)
        );
    }
}
