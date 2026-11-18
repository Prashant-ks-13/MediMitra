package com.medimitra.common.contracts.event;

import java.time.Instant;
import java.util.UUID;

/** Published by Scheduling on {@code appointment.checked_in}; grants the doctor time-boxed record access. */
public record AppointmentCheckedInV1(UUID appointmentId, UUID patientId, UUID doctorId, Instant checkedInAt) {
}
