package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by Scheduling on {@code appointment.cancelled}; triggers the refund saga when a payment was captured. */
public record AppointmentCancelledV1(UUID appointmentId, UUID cancelledBy, String reason, Instant cancelledAt) {
}
