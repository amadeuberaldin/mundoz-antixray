package com.amadeu.mundozantixray.domain.protection;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ProtectionCategory;

import java.util.Map;
import java.util.Optional;

public final class DefaultBlockProtectionRule
        implements BlockProtectionRule {

    private static final Map<BlockIdentity, ProtectionCategory> RULES =
            Map.ofEntries(
                    Map.entry(
                            BlockIdentity.DIAMOND_ORE,
                            ProtectionCategory.RESOURCE
                    ),
                    Map.entry(
                            BlockIdentity.ANCIENT_DEBRIS,
                            ProtectionCategory.RESOURCE
                    ),
                    Map.entry(
                            BlockIdentity.LAVA,
                            ProtectionCategory.UNDERGROUND_VISIBILITY
                    )
            );

    @Override
    public Optional<ProtectionCategory> categoryFor(
            BlockIdentity block
    ) {
        return Optional.ofNullable(RULES.get(block));
    }
}
