package com.medimitra.common.contracts.event;

import com.medimitra.common.core.Region;
import com.medimitra.common.core.UserId;
import java.time.Instant;

/** Published by Identity &amp; Access on {@code user.registered} after sign-up completes. */
public record UserRegisteredV1(UserId userId, UserRole role, Region region, Instant registeredAt) {
}
