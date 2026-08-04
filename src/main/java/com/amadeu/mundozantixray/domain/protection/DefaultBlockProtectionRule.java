package com.amadeu.mundozantixray.domain.protection;

import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ProtectionCategory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefaultBlockProtectionRule
        implements BlockProtectionRule {

    private static final Map<BlockIdentity, ProtectionCategory> RULES = Map.ofEntries(
            Map.entry(BlockIdentity.COAL_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.DEEPSLATE_COAL_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.COPPER_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.DEEPSLATE_COPPER_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.IRON_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.DEEPSLATE_IRON_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.GOLD_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.DEEPSLATE_GOLD_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.REDSTONE_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.DEEPSLATE_REDSTONE_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.EMERALD_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.DEEPSLATE_EMERALD_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.LAPIS_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.DEEPSLATE_LAPIS_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.DIAMOND_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.DEEPSLATE_DIAMOND_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.NETHER_GOLD_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.NETHER_QUARTZ_ORE, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.ANCIENT_DEBRIS, ProtectionCategory.RESOURCE),
            Map.entry(BlockIdentity.LAVA, ProtectionCategory.UNDERGROUND_VISIBILITY)
    );

    @Override
    public Optional<ProtectionCategory> categoryFor(
            BlockIdentity block
    ) {
        Objects.requireNonNull(
                block,
                "block"
        );
        return Optional.ofNullable(RULES.get(block));
    }
}
