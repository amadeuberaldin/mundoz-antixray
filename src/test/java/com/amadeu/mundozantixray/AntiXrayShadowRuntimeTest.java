package com.amadeu.mundozantixray;

import com.amadeu.mundozantixray.application.runtime.RuntimeDecisionComparison;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AntiXrayShadowRuntimeTest {
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void disabledShadowEvaluationDoesNotReadRuntimeInputs() {
        assertEquals(
                RuntimeDecisionComparison.V2_CANNOT_EVALUATE,
                AntiXrayShadowRuntime.SectionEvaluation.disabled().compare(
                        null,
                        null,
                        null,
                        null,
                        true
                )
        );
    }
}
