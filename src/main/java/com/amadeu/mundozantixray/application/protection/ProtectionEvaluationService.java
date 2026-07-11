package com.amadeu.mundozantixray.application.protection;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;
import com.amadeu.mundozantixray.domain.protection.ProtectionPolicy;

public final class ProtectionEvaluationService {

    private final ProtectionPolicy policy;

    public ProtectionEvaluationService(
            ProtectionPolicy policy
    ) {
        this.policy = policy;
    }

    public ProtectionDecision evaluate(
            BlockIdentity block
    ) {
        return policy.evaluate(block);
    }
}
