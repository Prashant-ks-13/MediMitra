package com.medimitra.patientprofile.application;

import com.medimitra.common.contracts.event.PatientCreatedV1;
import com.medimitra.common.contracts.event.UserRegisteredV1;
import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.IdGenerator;
import com.medimitra.common.core.UserId;
import com.medimitra.common.messaging.EventEnvelope;
import com.medimitra.common.messaging.EventPublisher;
import com.medimitra.patientprofile.application.port.PatientRepository;
import com.medimitra.patientprofile.domain.Patient;
import java.time.Instant;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/** Consumes {@code user.registered} and creates the bare {@link Patient} record (README section 6/9.1). */
public class PatientCreationService {

    private final PatientRepository repository;
    private final EventPublisher eventPublisher;
    private final ClockProvider clockProvider;
    private final IdGenerator idGenerator;

    public PatientCreationService(
            PatientRepository repository, EventPublisher eventPublisher, ClockProvider clockProvider, IdGenerator idGenerator) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.clockProvider = clockProvider;
        this.idGenerator = idGenerator;
    }

    /** Only {@code PATIENT} registrations get a Patient row; caregivers access an existing patient via a family link instead. */
    @Transactional
    public void createFromRegistration(UserRegisteredV1 event) {
        if (event.role() != UserRole.PATIENT) {
            return;
        }
        UUID userId = event.userId().value();
        if (repository.existsByUserId(userId)) {
            return;
        }
        Instant now = clockProvider.now();
        UUID patientId = idGenerator.newId();
        repository.save(Patient.newlyRegistered(patientId, userId, event.region(), now));
        eventPublisher.publish(EventEnvelope.<PatientCreatedV1>builder()
                .eventId(idGenerator.newId())
                .eventType("created")
                .aggregateType("patient")
                .aggregateId(patientId.toString())
                .region(event.region())
                .occurredAt(now)
                .producer("patient-profile")
                .payload(new PatientCreatedV1(patientId, UserId.of(userId), event.region(), now))
                .build());
    }
}
