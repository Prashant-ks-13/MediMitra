/**
 * Shared event infrastructure: {@link com.medimitra.common.messaging.EventEnvelope}, the
 * {@link com.medimitra.common.messaging.EventPublisher} port, the transactional outbox, Kafka
 * publishing and the idempotent-consumer dedupe store. Opt in with
 * {@link com.medimitra.common.messaging.EnableMediMitraMessaging}; nothing here is a hidden bean.
 */
package com.medimitra.common.messaging;
