package com.medimitra.identityaccess.application;

import com.medimitra.identityaccess.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;

/** Port the application layer depends on; {@code infrastructure.persistence} provides the JPA adapter. */
public interface UserAccountRepository {
    boolean existsByEmail(String email);

    Optional<UUID> findIdByEmail(String email);

    void save(UserAccount account);
}
