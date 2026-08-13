package com.amadeu.mundozantixray;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ShadowValidationExplicitlyDisabledTest {
    @Test
    void explicitFalseStartupPropertyDisablesDiagnostics() {
        assertFalse(ShadowRuntimeDiagnostics.isEnabled());
    }
}
