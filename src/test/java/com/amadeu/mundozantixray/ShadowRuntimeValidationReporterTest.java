package com.amadeu.mundozantixray;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ShadowRuntimeValidationReporterTest {
    @Test
    void reportsAtMostOnce() {
        Logger logger = mock(Logger.class);
        ShadowRuntimeValidationReporter reporter = new ShadowRuntimeValidationReporter();
        ShadowRuntimeDiagnostics.Summary summary = summary();

        reporter.report(logger, summary);
        reporter.report(logger, summary);

        verify(logger, times(1)).info(anyString(), any(Object[].class));
    }

    @Test
    void loggingFailureCannotEscape() {
        Logger logger = mock(Logger.class);
        doThrow(new IllegalStateException("logger unavailable"))
                .when(logger).info(anyString(), any(Object[].class));

        assertDoesNotThrow(() ->
                new ShadowRuntimeValidationReporter().report(logger, summary()));
    }

    private static ShadowRuntimeDiagnostics.Summary summary() {
        return new ShadowRuntimeDiagnostics.Summary(
                1L, 2L, 2L, 1L, 3L, 3L,
                1L, 0L, 0L, 1L, 0L, 0L, 0L, 0L);
    }
}
