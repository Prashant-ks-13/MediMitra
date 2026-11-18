package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Published by Patient Profile on {@code patient.updated}; {@code changedFields} names only what changed. */
public record PatientUpdatedV1(UUID patientId, List<String> changedFields, Instant updatedAt) {
}
