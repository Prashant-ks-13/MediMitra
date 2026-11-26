package com.medimitra.identityaccess.api;

import com.medimitra.common.contracts.event.UserRole;
import com.medimitra.common.core.Region;
import com.medimitra.identityaccess.domain.UserAccount;
import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(UUID userId, String email, UserRole role, Region region, Instant createdAt) {

    public static UserProfileResponse from(UserAccount account) {
        return new UserProfileResponse(account.id(), account.email(), account.role(), account.region(), account.createdAt());
    }
}
