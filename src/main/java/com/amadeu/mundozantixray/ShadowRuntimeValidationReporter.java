package com.amadeu.mundozantixray;

import org.slf4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

final class ShadowRuntimeValidationReporter {
    private final AtomicBoolean reported = new AtomicBoolean();

    void report(Logger logger, ShadowRuntimeDiagnostics.Summary summary) {
        try {
            Objects.requireNonNull(logger, "logger");
            Objects.requireNonNull(summary, "summary");
            if (!reported.compareAndSet(false, true)) {
                return;
            }

            logger.info(
                    "AntiXray v2 shadow validation summary: "
                            + "sectionInitialization[count={},totalNanos={},maxNanos={}], "
                            + "evaluation[count={},totalNanos={},maxNanos={}], "
                            + "bothReveal={}, bothHide={}, v1HidesV2Reveals={}, "
                            + "v1RevealsV2Hides={}, "
                            + "agreement={}, disagreement={}, v2Observed={}, "
                            + "v2NotObserved={}, unsupported={}, unavailable={}, "
                            + "missingReplacement={}, failure={}",
                    summary.sectionInitializationCount(),
                    summary.sectionInitializationTotalNanos(),
                    summary.sectionInitializationMaxNanos(),
                    summary.evaluationCount(),
                    summary.evaluationTotalNanos(),
                    summary.evaluationMaxNanos(),
                    summary.bothReveal(),
                    summary.bothHide(),
                    summary.v1HidesV2Reveals(),
                    summary.v1RevealsV2Hides(),
                    summary.agreements(),
                    summary.disagreements(),
                    summary.v2Observed(),
                    summary.v2NotObserved(),
                    summary.unsupported(),
                    summary.unavailable(),
                    summary.missingReplacement(),
                    summary.failures()
            );
        } catch (Throwable diagnosticFailure) {
            // Validation reporting must never affect server shutdown.
        }
    }
}
