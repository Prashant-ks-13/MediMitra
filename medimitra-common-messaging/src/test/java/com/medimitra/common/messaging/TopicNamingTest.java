package com.medimitra.common.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.medimitra.common.core.Region;
import org.junit.jupiter.api.Test;

class TopicNamingTest {

    @Test
    void buildsTopicPerConvention() {
        assertEquals("medimitra.in.appointment.booked.v1", TopicNaming.topic(Region.IN, "appointment", "booked", 1));
    }

    @Test
    void buildsConsumerGroupPerConvention() {
        assertEquals("medimitra-notification-appointment", TopicNaming.consumerGroup("notification", "appointment"));
    }

    @Test
    void appendsDeadLetterSuffix() {
        assertEquals("medimitra.in.appointment.booked.v1.dlt", TopicNaming.deadLetter(TopicNaming.topic(Region.IN, "appointment", "booked", 1)));
    }
}
