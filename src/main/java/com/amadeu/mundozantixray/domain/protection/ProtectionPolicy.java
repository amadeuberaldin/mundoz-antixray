package com.amadeu.mundozantixray.domain.protection;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;

public interface ProtectionPolicy {

    ProtectionDecision evaluate(
            BlockIdentity block
    );
}
