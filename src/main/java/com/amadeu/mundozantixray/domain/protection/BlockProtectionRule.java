package com.amadeu.mundozantixray.domain.protection;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ProtectionCategory;

import java.util.Optional;

public interface BlockProtectionRule {

    Optional<ProtectionCategory> categoryFor(
            BlockIdentity block
    );
}
