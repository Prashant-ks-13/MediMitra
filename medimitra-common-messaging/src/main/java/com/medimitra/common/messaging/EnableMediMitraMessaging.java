package com.medimitra.common.messaging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Opt-in switch for the outbox relay, the idempotent-consumer dedupe store, and Kafka publishing.
 * No bean in this module is registered unless a service declares this on one of its own
 * {@code @Configuration} classes, per the common-module rule against hidden beans.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableScheduling
@EntityScan(basePackages = "com.medimitra.common.messaging")
@EnableJpaRepositories(basePackages = "com.medimitra.common.messaging")
@Import(MediMitraMessagingAutoConfiguration.class)
public @interface EnableMediMitraMessaging {
}
