package com.medimitra.patientprofile;

import com.medimitra.common.messaging.EnableMediMitraMessaging;
import com.medimitra.common.security.EnableMediMitraSecurity;
import com.medimitra.common.web.EnableMediMitraWeb;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * {@code @EnableMediMitraMessaging} carries its own {@code @EntityScan}/{@code @EnableJpaRepositories}
 * scoped only to the outbox package — redeclaring both here (combined with this service's own
 * persistence package) is required, or this service's own JPA entities/repositories would never
 * be registered (see medimitra-identity-access-service, which hit this same issue first).
 */
@SpringBootApplication
@EnableMediMitraMessaging
@EnableMediMitraSecurity
@EnableMediMitraWeb
@EntityScan(basePackages = {"com.medimitra.patientprofile.infrastructure.persistence", "com.medimitra.common.messaging"})
@EnableJpaRepositories(basePackages = {"com.medimitra.patientprofile.infrastructure.persistence", "com.medimitra.common.messaging"})
public class PatientProfileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientProfileServiceApplication.class, args);
    }
}
