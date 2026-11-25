package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published by Lab Order on {@code lab_result.published} once signed results are ingested. Medical
 * Records saves the observations (no OCR needed); Notification alerts the patient, caregivers and
 * the referring doctor.
 */
public record LabResultPublishedV1(UUID labOrderId, UUID patientId, List<UUID> observationIds, Instant publishedAt) {
}
