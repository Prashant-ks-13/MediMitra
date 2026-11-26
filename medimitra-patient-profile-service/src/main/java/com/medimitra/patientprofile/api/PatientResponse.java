package com.medimitra.patientprofile.api;

import com.medimitra.patientprofile.domain.Patient;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(UUID id, UUID userId, String fullName, LocalDate dateOfBirth, String gender, String abhaId) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.id(), patient.userId(), patient.fullName(), patient.dateOfBirth(), patient.gender(), patient.abhaId());
    }
}
