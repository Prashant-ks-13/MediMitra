package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by Medical Records on {@code record.created} when an encounter's notes/prescription are saved. */
public record RecordCreatedV1(
        UUID encounterId, UUID patientId, UUID doctorId, UUID appointmentId, Instant visitAt, Instant createdAt) {
}
