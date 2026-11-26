package com.medimitra.patientprofile.application;

import com.medimitra.common.contracts.event.PatientUpdatedV1;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.IdGenerator;
import com.medimitra.common.core.exception.ConsentRequiredException;
import com.medimitra.common.core.exception.ResourceNotFoundException;
import com.medimitra.common.messaging.EventEnvelope;
import com.medimitra.common.messaging.EventPublisher;
import com.medimitra.patientprofile.PatientErrorCodes;
import com.medimitra.patientprofile.application.port.PatientRepository;
import com.medimitra.patientprofile.domain.Patient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/**
 * Backs {@code GET/PATCH /patients/{id}}. v1 is owner-only: a caller may read/write only their own
 * record. Caregiver/doctor access via {@code @RequiresConsent} is deferred until Consent &amp; Access
 * exists to answer the check — {@link ConsentRequiredException} here is the same exception that
 * check will eventually throw on denial, so this is forward-compatible, not a stand-in hack.
 */
public class PatientProfileService {

    private final PatientRepository repository;
    private final EventPublisher eventPublisher;
    private final ClockProvider clockProvider;
    private final IdGenerator idGenerator;

    public PatientProfileService(
            PatientRepository repository, EventPublisher eventPublisher, ClockProvider clockProvider, IdGenerator idGenerator) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.clockProvider = clockProvider;
        this.idGenerator = idGenerator;
    }

    public Patient getProfile(UUID patientId, UUID requestingUserId) {
        Patient patient = find(patientId);
        requireOwner(patient, requestingUserId);
        return patient;
    }

    @Transactional
    public Patient updateProfile(UUID patientId, UUID requestingUserId, String fullName, LocalDate dateOfBirth, String gender, String abhaId) {
        Patient existing = find(patientId);
        requireOwner(existing, requestingUserId);
        Instant now = clockProvider.now();
        Patient updated = existing.withDemographics(fullName, dateOfBirth, gender, abhaId, now);
        repository.save(updated);
        eventPublisher.publish(EventEnvelope.<PatientUpdatedV1>builder()
                .eventId(idGenerator.newId())
                .eventType("updated")
                .aggregateType("patient")
                .aggregateId(patientId.toString())
                .region(updated.region())
                .occurredAt(now)
                .producer("patient-profile")
                .payload(new PatientUpdatedV1(patientId, changedFields(existing, updated), now))
                .build());
        return updated;
    }

    private Patient find(UUID patientId) {
        return repository
                .findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(PatientErrorCodes.PATIENT_NOT_FOUND, "no patient " + patientId));
    }

    private static void requireOwner(Patient patient, UUID requestingUserId) {
        if (!patient.isOwnedBy(requestingUserId)) {
            throw new ConsentRequiredException(PatientErrorCodes.CONSENT_REQUIRED, "no consent to access patient " + patient.id());
        }
    }

    private static List<String> changedFields(Patient before, Patient after) {
        List<String> changed = new ArrayList<>();
        if (!Objects.equals(before.fullName(), after.fullName())) {
            changed.add("fullName");
        }
        if (!Objects.equals(before.dateOfBirth(), after.dateOfBirth())) {
            changed.add("dateOfBirth");
        }
        if (!Objects.equals(before.gender(), after.gender())) {
            changed.add("gender");
        }
        if (!Objects.equals(before.abhaId(), after.abhaId())) {
            changed.add("abhaId");
        }
        return changed;
    }
}
