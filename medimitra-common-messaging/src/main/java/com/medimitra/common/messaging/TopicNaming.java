package com.medimitra.common.messaging;

import com.medimitra.common.core.Region;
import java.util.Objects;

/** Builds Kafka topic and consumer-group names per the naming convention in the HLD/LLD, section 7.5. */
public final class TopicNaming {

    private TopicNaming() {
    }

    /** {@code medimitra.<region>.<aggregate>.<event>.v<n>}, e.g. {@code medimitra.in.appointment.booked.v1}. */
    public static String topic(Region region, String aggregate, String event, int version) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(aggregate, "aggregate");
        Objects.requireNonNull(event, "event");
        return "medimitra." + region.name().toLowerCase() + "." + aggregate + "." + event + ".v" + version;
    }

    /** {@code medimitra-<service>-<purpose>}, e.g. {@code medimitra-notification-appointment}. */
    public static String consumerGroup(String service, String purpose) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(purpose, "purpose");
        return "medimitra-" + service + "-" + purpose;
    }

    /** Spring Kafka's dead-letter suffix, appended to a topic already built by {@link #topic}. */
    public static String deadLetter(String topic) {
        Objects.requireNonNull(topic, "topic");
        return topic + ".dlt";
    }
}
