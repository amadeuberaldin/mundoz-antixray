package com.amadeu.mundozantixray.domain.protection;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;

public final class DefaultProtectionPolicy
        implements ProtectionPolicy {

    private final BlockProtectionRule rule;

    public DefaultProtectionPolicy(
            BlockProtectionRule rule
    ) {
        this.rule = rule;
    }

    @Override
    public ProtectionDecision evaluate(
            BlockIdentity block
    ) {
        return rule.categoryFor(block).isPresent()
                ? ProtectionDecision.PROTECTED
                : ProtectionDecision.NOT_PROTECTED;
    }
}
