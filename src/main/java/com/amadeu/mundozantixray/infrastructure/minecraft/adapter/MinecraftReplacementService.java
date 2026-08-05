package com.amadeu.mundozantixray.infrastructure.minecraft.adapter;

import com.amadeu.mundozantixray.application.replacement.ReplacementIntegrationService;
import com.amadeu.mundozantixray.domain.model.BlockIdentity;
import com.amadeu.mundozantixray.domain.model.ReplacementDecision;
import com.amadeu.mundozantixray.domain.model.ReplacementResult;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Objects;

public final class MinecraftReplacementService {

    private final ReplacementIntegrationService integrationService;
    private final ReplacementContextMapper contextMapper;

    public MinecraftReplacementService(
            ReplacementIntegrationService integrationService,
            ReplacementContextMapper contextMapper
    ) {
        this.integrationService = Objects.requireNonNull(
                integrationService,
                "integrationService"
        );
        this.contextMapper = Objects.requireNonNull(
                contextMapper,
                "contextMapper"
        );
    }

    public BlockState replacementFor(
            BlockState original,
            List<BlockState> availableStates
    ) {
        Objects.requireNonNull(original, "original");
        Objects.requireNonNull(availableStates, "availableStates");

        return BlockIdentityMapper.map(original)
                .map(block -> replacementForSupportedBlock(
                        original,
                        block,
                        availableStates
                ))
                .orElse(original);
    }

    private BlockState replacementForSupportedBlock(
            BlockState original,
            BlockIdentity block,
            List<BlockState> availableStates
    ) {
        ReplacementResult result = integrationService.evaluate(
                block,
                contextMapper.mapAvailableRepresentations(availableStates)
        );

        if (result.decision() == ReplacementDecision.KEEP_VISIBLE) {
            return original;
        }

        return ReplacementRepresentationMapper.map(
                result.representation().orElseThrow()
        );
    }
}
