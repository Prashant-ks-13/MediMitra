package com.medimitra.identityaccess.application;

import com.medimitra.common.core.exception.ResourceNotFoundException;
import com.medimitra.identityaccess.IdentityErrorCodes;
import com.medimitra.identityaccess.domain.UserAccount;
import java.util.UUID;

/** Backs {@code GET /users/me}. */
public class UserProfileService {

    private final UserAccountRepository repository;

    public UserProfileService(UserAccountRepository repository) {
        this.repository = repository;
    }

    public UserAccount getProfile(UUID userId) {
        return repository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(IdentityErrorCodes.ACCOUNT_NOT_FOUND, "no account for " + userId));
    }
}
