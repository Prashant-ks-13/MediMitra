package com.medimitra.common.contracts.event;

import com.medimitra.common.core.Region;
import java.time.Instant;
import java.util.UUID;

/** Published by Provider Directory on {@code provider.verified} once admin review approves a doctor or lab. */
public record ProviderVerifiedV1(UUID providerId, ProviderType providerType, Region region, Instant verifiedAt) {
}
