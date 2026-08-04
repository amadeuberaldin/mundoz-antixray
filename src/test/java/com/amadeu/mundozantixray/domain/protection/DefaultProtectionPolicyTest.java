package com.amadeu.mundozantixray.domain.protection;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ProtectionCategory;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultProtectionPolicyTest {
    @Test void everyAcceptedIdentityIsProtected() {
        var policy = new DefaultProtectionPolicy(new DefaultBlockProtectionRule());
        for (BlockIdentity identity : EnumSet.allOf(BlockIdentity.class)) {
            assertEquals(ProtectionDecision.PROTECTED, policy.evaluate(identity), identity.name());
        }
    }

    @Test void validUnmappedIdentityIsNotProtected() {
        var policy = new DefaultProtectionPolicy(block -> Optional.empty());
        assertEquals(ProtectionDecision.NOT_PROTECTED, policy.evaluate(BlockIdentity.COAL_ORE));
    }

    @Test void nullRuleIsRejected() {
        assertThrows(NullPointerException.class, () -> new DefaultProtectionPolicy(null));
    }

    @Test void nullIdentityIsRejectedBeforeRuleEvaluation() {
        var policy = new DefaultProtectionPolicy(block -> Optional.of(ProtectionCategory.RESOURCE));
        assertThrows(NullPointerException.class, () -> policy.evaluate(null));
    }
}
