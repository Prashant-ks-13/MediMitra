package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Published by Medical Records on {@code observation.recorded}. Triggers AI Insights to re-run trend
 * analysis for this patient and test; the value itself is fetched from Medical Records, not carried here.
 */
public record ObservationRecordedV1(
        UUID observationId, UUID patientId, String testCode, String flag, UUID sourceDocumentId, Instant observedAt) {
}
