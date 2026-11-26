package com.medimitra.identityaccess.application;

import com.medimitra.identityaccess.domain.UserAccount;

/** Port the application layer depends on; {@code infrastructure.persistence} provides the JPA adapter. */
public interface UserAccountRepository {
    boolean existsByEmail(String email);

    void save(UserAccount account);
}
