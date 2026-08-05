package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.application.protection.ProtectionEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementIntegrationService;
import com.amadeu.mundozantixray.domain.protection.DefaultBlockProtectionRule;
import com.amadeu.mundozantixray.domain.protection.DefaultProtectionPolicy;
import com.amadeu.mundozantixray.domain.replacement.DefaultReplacementPolicy;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MinecraftSectionReplacementIntegrationTest {

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private final MinecraftSectionReplacementCandidateSource candidateSource =
            new MinecraftSectionReplacementCandidateSource();
    private final MinecraftReplacementService replacementService =
            new MinecraftReplacementService(
                    new ReplacementIntegrationService(
                            new ProtectionEvaluationService(
                                    new DefaultProtectionPolicy(
                                            new DefaultBlockProtectionRule()
                                    )
                            ),
                            new ReplacementEvaluationService(
                                    new DefaultReplacementPolicy()
                            )
                    ),
                    new ReplacementContextMapper()
            );

    @Test
    void sectionCandidatesDriveTheInactiveReplacementFlow() {
        List<BlockState> candidates = candidatesFrom(
                Set.of(
                        Blocks.TUFF.defaultBlockState(),
                        Blocks.STONE.defaultBlockState(),
                        Blocks.DIAMOND_ORE.defaultBlockState()
                )
        );

        assertEquals(
                Blocks.STONE.defaultBlockState(),
                replacementService.replacementFor(
                        Blocks.DIAMOND_ORE.defaultBlockState(),
                        candidates
                )
        );
    }

    @Test
    void sectionWithoutAcceptedCandidateKeepsOriginalVisible() {
        BlockState original = Blocks.ANCIENT_DEBRIS.defaultBlockState();

        assertSame(
                original,
                replacementService.replacementFor(
                        original,
                        candidatesFrom(
                                Set.of(
                                        Blocks.BEDROCK.defaultBlockState(),
                                        Blocks.OBSIDIAN.defaultBlockState()
                                )
                        )
                )
        );
    }

    private List<BlockState> candidatesFrom(
            Set<BlockState> sectionStates
    ) {
        return candidateSource.candidatesMatching(sectionStates::contains);
    }
}
