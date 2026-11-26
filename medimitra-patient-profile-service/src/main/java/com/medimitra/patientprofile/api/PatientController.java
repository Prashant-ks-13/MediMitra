package com.medimitra.patientprofile.api;

import com.medimitra.common.security.AuthenticatedPrincipal;
import com.medimitra.patientprofile.application.PatientProfileService;
import com.medimitra.patientprofile.domain.Patient;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code /api/v1/patients/{id}} — v1 is owner-only (see {@link PatientProfileService}'s Javadoc). */
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientProfileService service;

    public PatientController(PatientProfileService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public PatientResponse get(@PathVariable UUID id) {
        Patient patient = service.getProfile(id, currentUserId());
        return PatientResponse.from(patient);
    }

    @PatchMapping("/{id}")
    public PatientResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePatientRequest request) {
        Patient updated =
                service.updateProfile(id, currentUserId(), request.fullName(), request.dateOfBirth(), request.gender(), request.abhaId());
        return PatientResponse.from(updated);
    }

    private static UUID currentUserId() {
        AuthenticatedPrincipal principal = (AuthenticatedPrincipal) SecurityContextHolder.getContext().getAuthentication();
        return principal.subject().value();
    }
}
