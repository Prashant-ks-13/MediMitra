package com.medimitra.identityaccess;

import com.medimitra.common.messaging.EnableMediMitraMessaging;
import com.medimitra.common.security.EnableMediMitraSecurity;
import com.medimitra.common.web.EnableMediMitraWeb;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * {@code @EnableMediMitraMessaging} carries its own {@code @EntityScan}/{@code @EnableJpaRepositories}
 * scoped only to the outbox package — only one such annotation takes effect per context, so without
 * redeclaring both here (combined with this service's own persistence package) this service's own
 * JPA entities and repositories would silently never be registered.
 */
@SpringBootApplication
@EnableMediMitraMessaging
@EnableMediMitraSecurity
@EnableMediMitraWeb
@EntityScan(basePackages = {"com.medimitra.identityaccess.infrastructure.persistence", "com.medimitra.common.messaging"})
@EnableJpaRepositories(basePackages = {"com.medimitra.identityaccess.infrastructure.persistence", "com.medimitra.common.messaging"})
public class IdentityAccessServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityAccessServiceApplication.class, args);
    }
}
