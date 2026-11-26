package com.medimitra.patientprofile;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PatientProfileServiceApplicationTests {

    @Test
    void contextLoads() {
        // fails if any bean (security, messaging, Kafka listener, JPA) is wired incorrectly
    }
}
