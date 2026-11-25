package com.medimitra.common.messaging.outbox;

/** Lifecycle of one outbox row; {@code FAILED} means the relay gave up after {@code maxAttempts} and needs manual review. */
public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
