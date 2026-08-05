package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.application.protection.ProtectionEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementEvaluationService;
import com.amadeu.mundozantixray.application.replacement.ReplacementIntegrationService;
import com.amadeu.mundozantixray.domain.model.ProtectionDecision;
import com.amadeu.mundozantixray.domain.protection.DefaultBlockProtectionRule;
import com.amadeu.mundozantixray.domain.protection.DefaultProtectionPolicy;
import com.amadeu.mundozantixray.domain.replacement.DefaultReplacementPolicy;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinecraftReplacementServiceTest {

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void protectedMinecraftBlockReceivesHighestPriorityAvailableReplacement() {
        MinecraftReplacementService service = defaultService();

        assertEquals(
                Blocks.STONE.defaultBlockState(),
                service.replacementFor(
                        Blocks.DIAMOND_ORE.defaultBlockState(),
                        List.of(
                                Blocks.TUFF.defaultBlockState(),
                                Blocks.STONE.defaultBlockState()
                        )
                )
        );
    }

    @Test
    void protectedMinecraftBlockRemainsVisibleWithoutSafeReplacement() {
        MinecraftReplacementService service = defaultService();
        BlockState original = Blocks.ANCIENT_DEBRIS.defaultBlockState();

        assertSame(
                original,
                service.replacementFor(
                        original,
                        List.of(
                                Blocks.AIR.defaultBlockState(),
                                Blocks.BEDROCK.defaultBlockState(),
                                Blocks.OBSIDIAN.defaultBlockState()
                        )
                )
        );
    }

    @Test
    void unsupportedMinecraftBlockRemainsVisible() {
        MinecraftReplacementService service = defaultService();
        BlockState original = Blocks.OAK_PLANKS.defaultBlockState();

        assertSame(
                original,
                service.replacementFor(
                        original,
                        List.of(Blocks.STONE.defaultBlockState())
                )
        );
    }

    @Test
    void notProtectedDecisionPreservesOriginalMinecraftBlock() {
        MinecraftReplacementService service = new MinecraftReplacementService(
                new ReplacementIntegrationService(
                        new ProtectionEvaluationService(
                                block -> ProtectionDecision.NOT_PROTECTED
                        ),
                        new ReplacementEvaluationService(
                                new DefaultReplacementPolicy()
                        )
                ),
                new ReplacementContextMapper()
        );
        BlockState original = Blocks.DIAMOND_ORE.defaultBlockState();

        assertSame(
                original,
                service.replacementFor(
                        original,
                        List.of(Blocks.STONE.defaultBlockState())
                )
        );
    }

    @Test
    void nullDependenciesAndInputsAreRejected() {
        ReplacementIntegrationService integrationService =
                defaultIntegrationService();
        ReplacementContextMapper contextMapper =
                new ReplacementContextMapper();

        assertThrows(
                NullPointerException.class,
                () -> new MinecraftReplacementService(null, contextMapper)
        );
        assertThrows(
                NullPointerException.class,
                () -> new MinecraftReplacementService(
                        integrationService,
                        null
                )
        );

        MinecraftReplacementService service =
                new MinecraftReplacementService(
                        integrationService,
                        contextMapper
                );

        assertThrows(
                NullPointerException.class,
                () -> service.replacementFor(null, List.of())
        );
        assertThrows(
                NullPointerException.class,
                () -> service.replacementFor(
                        Blocks.DIAMOND_ORE.defaultBlockState(),
                        null
                )
        );
    }

    private static MinecraftReplacementService defaultService() {
        return new MinecraftReplacementService(
                defaultIntegrationService(),
                new ReplacementContextMapper()
        );
    }

    private static ReplacementIntegrationService defaultIntegrationService() {
        return new ReplacementIntegrationService(
                new ProtectionEvaluationService(
                        new DefaultProtectionPolicy(
                                new DefaultBlockProtectionRule()
                        )
                ),
                new ReplacementEvaluationService(
                        new DefaultReplacementPolicy()
                )
        );
    }
}
