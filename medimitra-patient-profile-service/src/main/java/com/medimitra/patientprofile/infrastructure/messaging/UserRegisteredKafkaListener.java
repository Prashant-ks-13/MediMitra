package com.medimitra.patientprofile.infrastructure.messaging;

import com.medimitra.common.contracts.event.UserRegisteredV1;
import com.medimitra.common.messaging.EventEnvelope;
import com.medimitra.common.messaging.EventEnvelopeCodec;
import org.springframework.kafka.annotation.KafkaListener;

/** Bridges the raw Kafka message onto {@link UserRegisteredConsumer}; the topic/group come from this service's own config. */
public class UserRegisteredKafkaListener {

    private final EventEnvelopeCodec codec;
    private final UserRegisteredConsumer consumer;

    public UserRegisteredKafkaListener(EventEnvelopeCodec codec, UserRegisteredConsumer consumer) {
        this.codec = codec;
        this.consumer = consumer;
    }

    @KafkaListener(topics = "${medimitra.patient-profile.topics.user-registered}", groupId = "${medimitra.patient-profile.topics.user-registered-group}")
    public void onMessage(String rawEnvelopeJson) {
        EventEnvelope<UserRegisteredV1> envelope = codec.decode(rawEnvelopeJson, UserRegisteredV1.class);
        consumer.consume(envelope);
    }
}
