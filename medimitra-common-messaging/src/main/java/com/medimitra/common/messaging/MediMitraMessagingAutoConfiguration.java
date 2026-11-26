package com.medimitra.common.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.messaging.kafka.KafkaEventPublisher;
import com.medimitra.common.messaging.outbox.OutboxEventPublisher;
import com.medimitra.common.messaging.outbox.OutboxEventRepository;
import com.medimitra.common.messaging.outbox.OutboxRelay;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/** Wires the beans {@link EnableMediMitraMessaging} opts a service into; nothing here is registered otherwise. */
@Configuration
@EnableConfigurationProperties(MediMitraMessagingProperties.class)
public class MediMitraMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ClockProvider clockProvider() {
        return new SystemClockProvider();
    }

    @Bean
    @ConditionalOnMissingBean(name = "outboxKafkaTemplate")
    public KafkaTemplate<String, String> outboxKafkaTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> producerProps = kafkaProperties.buildProducerProperties(null);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaEventPublisher kafkaEventPublisher(KafkaTemplate<String, String> outboxKafkaTemplate, ObjectMapper objectMapper) {
        return new KafkaEventPublisher(outboxKafkaTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventPublisher eventPublisher(OutboxEventRepository repository, ObjectMapper objectMapper) {
        return new OutboxEventPublisher(repository, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxRelay outboxRelay(
            OutboxEventRepository repository,
            KafkaEventPublisher kafkaEventPublisher,
            ClockProvider clockProvider,
            MediMitraMessagingProperties properties) {
        return new OutboxRelay(
                repository, kafkaEventPublisher, clockProvider,
                properties.getRelay().getBatchSize(), properties.getRelay().getMaxAttempts());
    }

    /**
     * Applied automatically by Spring Boot's own listener-container-factory auto-configuration once
     * present as a bean: retries each failed delivery with backoff, then routes it to {@code <topic>.dlt}
     * for alerting, per the retry/dead-letter rule in the Kafka topics table.
     *
     * <p>Reuses {@code outboxKafkaTemplate} (rather than depending on Boot's own default
     * {@code KafkaTemplate<Object, Object>} bean) because {@code @ConditionalOnMissingBean(KafkaTemplate.class)}
     * matches by raw type regardless of generics — {@code outboxKafkaTemplate}'s mere presence already
     * suppresses that default bean, so a parameter typed {@code KafkaTemplate<Object, Object>} here would
     * never resolve. {@code DeadLetterPublishingRecoverer} accepts {@code KafkaOperations<?, ?>}, so the
     * narrower {@code <String, String>} template works fine.
     */
    @Bean
    @ConditionalOnMissingBean(CommonErrorHandler.class)
    public CommonErrorHandler commonErrorHandler(KafkaTemplate<String, String> outboxKafkaTemplate, MediMitraMessagingProperties properties) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                outboxKafkaTemplate,
                (record, ex) -> new TopicPartition(TopicNaming.deadLetter(record.topic()), record.partition()));
        ExponentialBackOff backOff =
                new ExponentialBackOff(properties.getRetry().getInitialIntervalMs(), properties.getRetry().getMultiplier());
        backOff.setMaxAttempts(properties.getRetry().getMaxAttempts());
        return new DefaultErrorHandler(recoverer, backOff);
    }
}
