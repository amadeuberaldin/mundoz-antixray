package com.amadeu.mundozantixray.application.replacement;

import com.amadeu.mundozantixray.application.protection.ProtectionEvaluationService;
import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;
import com.amadeu.mundozantixray.domain.model.ReplacementRepresentation;
import com.amadeu.mundozantixray.domain.model.ReplacementResult;
import com.amadeu.mundozantixray.domain.replacement.ReplacementContext;

import java.util.List;
import java.util.Objects;

public final class ReplacementIntegrationService {

    private final ProtectionEvaluationService protectionService;
    private final ReplacementEvaluationService replacementService;

    public ReplacementIntegrationService(
            ProtectionEvaluationService protectionService,
            ReplacementEvaluationService replacementService
    ) {
        this.protectionService = Objects.requireNonNull(
                protectionService,
                "protectionService"
        );
        this.replacementService = Objects.requireNonNull(
                replacementService,
                "replacementService"
        );
    }

    public ReplacementResult evaluate(
            BlockIdentity block,
            List<ReplacementRepresentation> availableRepresentations
    ) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(
                availableRepresentations,
                "availableRepresentations"
        );

        if (protectionService.evaluate(block) == ProtectionDecision.NOT_PROTECTED) {
            return ReplacementResult.keepVisible();
        }

        return replacementService.evaluate(
                new ReplacementContext(
                        block,
                        availableRepresentations
                )
        );
    }
}
