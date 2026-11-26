package com.medimitra.patientprofile.infrastructure.messaging;

import com.medimitra.common.contracts.event.UserRegisteredV1;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.messaging.EventEnvelope;
import com.medimitra.common.messaging.idempotency.AbstractIdempotentConsumer;
import com.medimitra.common.messaging.idempotency.ProcessedEventRepository;
import com.medimitra.patientprofile.application.PatientCreationService;

/** Dedupe-protected handler for {@code user.registered}, wired to the actual topic by {@link UserRegisteredKafkaListener}. */
public class UserRegisteredConsumer extends AbstractIdempotentConsumer<UserRegisteredV1> {

    private final PatientCreationService patientCreationService;

    public UserRegisteredConsumer(
            ProcessedEventRepository processedEventRepository, ClockProvider clockProvider, PatientCreationService patientCreationService) {
        super(processedEventRepository, clockProvider);
        this.patientCreationService = patientCreationService;
    }

    @Override
    protected void handle(EventEnvelope<UserRegisteredV1> event) {
        patientCreationService.createFromRegistration(event.payload());
    }

    @Override
    protected String consumerName() {
        return "patient-profile-user-registered";
    }
}
