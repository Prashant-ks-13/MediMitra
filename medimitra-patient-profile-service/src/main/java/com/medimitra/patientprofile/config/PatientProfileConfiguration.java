package com.medimitra.patientprofile.config;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.IdGenerator;
import com.medimitra.common.messaging.EventEnvelopeCodec;
import com.medimitra.common.messaging.EventPublisher;
import com.medimitra.common.messaging.idempotency.ProcessedEventRepository;
import com.medimitra.patientprofile.application.PatientCreationService;
import com.medimitra.patientprofile.application.PatientProfileService;
import com.medimitra.patientprofile.application.port.PatientRepository;
import com.medimitra.patientprofile.infrastructure.messaging.UserRegisteredConsumer;
import com.medimitra.patientprofile.infrastructure.messaging.UserRegisteredKafkaListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** {@code PatientRepository} itself is not wired here — its JPA adapter is {@code @Repository}-annotated and self-registering (see its Javadoc). */
@Configuration
public class PatientProfileConfiguration {

    @Bean
    public PatientCreationService patientCreationService(
            PatientRepository repository, EventPublisher eventPublisher, ClockProvider clockProvider, IdGenerator idGenerator) {
        return new PatientCreationService(repository, eventPublisher, clockProvider, idGenerator);
    }

    @Bean
    public PatientProfileService patientProfileService(
            PatientRepository repository, EventPublisher eventPublisher, ClockProvider clockProvider, IdGenerator idGenerator) {
        return new PatientProfileService(repository, eventPublisher, clockProvider, idGenerator);
    }

    @Bean
    public UserRegisteredConsumer userRegisteredConsumer(
            ProcessedEventRepository processedEventRepository, ClockProvider clockProvider, PatientCreationService patientCreationService) {
        return new UserRegisteredConsumer(processedEventRepository, clockProvider, patientCreationService);
    }

    @Bean
    public UserRegisteredKafkaListener userRegisteredKafkaListener(EventEnvelopeCodec codec, UserRegisteredConsumer consumer) {
        return new UserRegisteredKafkaListener(codec, consumer);
    }
}
