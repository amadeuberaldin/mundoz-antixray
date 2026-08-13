package com.amadeu.mundozantixray;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowValidationOnlyEnabledTest {
    @Test
    void validationActivationDoesNotEnableShadowExecution() {
        assertTrue(ShadowRuntimeDiagnostics.isEnabled());
        assertFalse(AntiXrayShadowRuntime.isEnabled());
    }
}
