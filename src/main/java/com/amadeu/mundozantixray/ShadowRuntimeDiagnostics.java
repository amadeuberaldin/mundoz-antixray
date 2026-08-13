package com.amadeu.mundozantixray;

import com.amadeu.mundozantixray.application.runtime.RuntimeDecisionComparison;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

final class ShadowRuntimeDiagnostics {
    static final String ENABLED_PROPERTY = "mundoz.antixray.v2-shadow-validation";
    private static final boolean ENABLED = resolveEnabled(
            () -> System.getProperty(ENABLED_PROPERTY));
    private static final Aggregate AGGREGATE = new Aggregate();

    private ShadowRuntimeDiagnostics() {}

    static boolean isEnabled() {
        return ENABLED;
    }

    static boolean parseEnabled(String propertyValue) {
        return Boolean.parseBoolean(propertyValue);
    }

    static boolean resolveEnabled(Supplier<String> propertyValue) {
        try {
            return parseEnabled(propertyValue.get());
        } catch (Throwable diagnosticFailure) {
            return false;
        }
    }

    static Aggregate aggregate() {
        return AGGREGATE;
    }

    static final class Aggregate {
        private final AtomicLong sectionInitializationCount = new AtomicLong();
        private final AtomicLong sectionInitializationTotalNanos = new AtomicLong();
        private final AtomicLong sectionInitializationMaxNanos = new AtomicLong();
        private final AtomicLong evaluationCount = new AtomicLong();
        private final AtomicLong evaluationTotalNanos = new AtomicLong();
        private final AtomicLong evaluationMaxNanos = new AtomicLong();
        private final AtomicLong agreements = new AtomicLong();
        private final AtomicLong disagreements = new AtomicLong();
        private final AtomicLong v2Observed = new AtomicLong();
        private final AtomicLong v2NotObserved = new AtomicLong();
        private final AtomicLong unsupported = new AtomicLong();
        private final AtomicLong unavailable = new AtomicLong();
        private final AtomicLong missingReplacement = new AtomicLong();
        private final AtomicLong failures = new AtomicLong();

        void recordSectionInitialization(long elapsedNanos) {
            recordTiming(sectionInitializationCount, sectionInitializationTotalNanos,
                    sectionInitializationMaxNanos, elapsedNanos);
        }

        void recordComparison(RuntimeDecisionComparison comparison, long elapsedNanos) {
            recordEvaluationTiming(elapsedNanos);
            recordComparisonCounts(comparison);
        }

        void recordUnsupported() {
            unsupported.incrementAndGet();
        }

        void recordUnavailable(RuntimeDecisionComparison comparison, long elapsedNanos) {
            recordEvaluationTiming(elapsedNanos);
            recordComparisonCounts(comparison);
            unavailable.incrementAndGet();
        }

        void recordMissingReplacement(
                RuntimeDecisionComparison comparison,
                long elapsedNanos
        ) {
            recordEvaluationTiming(elapsedNanos);
            recordComparisonCounts(comparison);
            missingReplacement.incrementAndGet();
        }

        void recordFailure(long elapsedNanos) {
            recordEvaluationTiming(elapsedNanos);
            failures.incrementAndGet();
        }

        void recordFailure() {
            failures.incrementAndGet();
        }

        Summary snapshot() {
            return new Summary(sectionInitializationCount.get(),
                    sectionInitializationTotalNanos.get(), sectionInitializationMaxNanos.get(),
                    evaluationCount.get(), evaluationTotalNanos.get(), evaluationMaxNanos.get(),
                    agreements.get(), disagreements.get(), v2Observed.get(), v2NotObserved.get(),
                    unsupported.get(), unavailable.get(), missingReplacement.get(), failures.get());
        }

        private void recordEvaluationTiming(long elapsedNanos) {
            recordTiming(evaluationCount, evaluationTotalNanos, evaluationMaxNanos, elapsedNanos);
        }

        private void recordComparisonCounts(RuntimeDecisionComparison comparison) {
            switch (comparison) {
                case BOTH_REVEAL -> {
                    agreements.incrementAndGet();
                    v2Observed.incrementAndGet();
                }
                case BOTH_HIDE -> {
                    agreements.incrementAndGet();
                    v2NotObserved.incrementAndGet();
                }
                case V1_HIDES_V2_REVEALS -> {
                    disagreements.incrementAndGet();
                    v2Observed.incrementAndGet();
                }
                case V1_REVEALS_V2_HIDES -> {
                    disagreements.incrementAndGet();
                    v2NotObserved.incrementAndGet();
                }
                case V2_CANNOT_EVALUATE -> failures.incrementAndGet();
            }
        }

        private static void recordTiming(AtomicLong count, AtomicLong total,
                                         AtomicLong maximum, long elapsedNanos) {
            count.incrementAndGet();
            total.addAndGet(elapsedNanos);
            maximum.accumulateAndGet(elapsedNanos, Math::max);
        }
    }

    record Summary(long sectionInitializationCount, long sectionInitializationTotalNanos,
                   long sectionInitializationMaxNanos, long evaluationCount,
                   long evaluationTotalNanos, long evaluationMaxNanos, long agreements,
                   long disagreements, long v2Observed, long v2NotObserved, long unsupported,
                   long unavailable, long missingReplacement, long failures) {}
}
