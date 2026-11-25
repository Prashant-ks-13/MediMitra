package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by Scheduling on {@code appointment.completed} once the doctor closes out the visit. */
public record AppointmentCompletedV1(UUID appointmentId, UUID encounterId, Instant completedAt) {
}
