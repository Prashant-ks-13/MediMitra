package com.medimitra.identityaccess.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountJpaEntity, UUID> {
    boolean existsByEmail(String email);

    Optional<UserAccountJpaEntity> findByEmail(String email);
}
