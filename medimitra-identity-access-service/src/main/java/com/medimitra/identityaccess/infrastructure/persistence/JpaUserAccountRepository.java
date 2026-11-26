package com.medimitra.identityaccess.infrastructure.persistence;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import com.medimitra.identityaccess.application.UserAccountRepository;
import com.medimitra.identityaccess.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;

/** Adapter: translates between the domain's {@link UserAccount} and the JPA entity. */
public class JpaUserAccountRepository implements UserAccountRepository {

    private final UserAccountJpaRepository jpaRepository;

    public JpaUserAccountRepository(UserAccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<UUID> findIdByEmail(String email) {
        return jpaRepository.findByEmail(email).map(UserAccountJpaEntity::getId);
    }

    @Override
    public void save(UserAccount account) {
        jpaRepository.save(new UserAccountJpaEntity(
                account.id(), account.email(), account.role().name(), account.region().name(), account.createdAt()));
    }
}
