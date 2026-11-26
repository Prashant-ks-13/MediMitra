package com.medimitra.identityaccess.infrastructure.persistence;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import com.medimitra.identityaccess.application.port.UserAccountRepository;
import com.medimitra.identityaccess.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adapter: translates between the domain's {@link UserAccount} and the JPA entity.
 *
 * <p>Self-registering via {@code @Repository} rather than an explicit {@code @Bean} in
 * {@code config} — a factory method in a different package couldn't reference the
 * package-private {@link UserAccountJpaRepository} constructor parameter type at all.
 */
@Repository
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
    public Optional<UserAccount> findById(UUID id) {
        return jpaRepository.findById(id).map(JpaUserAccountRepository::toDomain);
    }

    @Override
    public void save(UserAccount account) {
        jpaRepository.save(new UserAccountJpaEntity(
                account.id(), account.email(), account.role().name(), account.region().name(), account.createdAt()));
    }

    private static UserAccount toDomain(UserAccountJpaEntity entity) {
        return new UserAccount(
                entity.getId(), entity.getEmail(), UserRole.valueOf(entity.getRole()), Region.valueOf(entity.getRegion()),
                entity.getCreatedAt());
    }
}
