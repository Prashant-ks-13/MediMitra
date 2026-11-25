package com.medimitra.common.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from {@code medimitra.messaging.*}, per the common config-property naming convention. */
@ConfigurationProperties(prefix = "medimitra.messaging")
public class MediMitraMessagingProperties {

    private final Relay relay = new Relay();
    private final Retry retry = new Retry();

    public Relay getRelay() {
        return relay;
    }

    public Retry getRetry() {
        return retry;
    }

    /** The outbox relay: how many rows it dispatches per poll, how often, and when to give up on one. */
    public static class Relay {
        private int batchSize = 100;
        private long fixedDelayMs = 2000;
        private int maxAttempts = 10;

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public long getFixedDelayMs() {
            return fixedDelayMs;
        }

        public void setFixedDelayMs(long fixedDelayMs) {
            this.fixedDelayMs = fixedDelayMs;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
    }

    /** Consumer-side retry before a failed message is sent to its dead-letter topic. */
    public static class Retry {
        private int maxAttempts = 5;
        private long initialIntervalMs = 1000;
        private double multiplier = 2.0;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialIntervalMs() {
            return initialIntervalMs;
        }

        public void setInitialIntervalMs(long initialIntervalMs) {
            this.initialIntervalMs = initialIntervalMs;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }
}
