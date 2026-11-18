package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Published by Scheduling on {@code appointment.booked} once a slot is held. Notification sends the
 * confirmation; AI Insights starts building the doctor's pre-visit summary.
 */
public record AppointmentBookedV1(
        UUID appointmentId,
        UUID slotId,
        UUID patientId,
        UUID doctorId,
        UUID bookedBy,
        String mode,
        Instant holdExpiresAt,
        Instant bookedAt) {
}
