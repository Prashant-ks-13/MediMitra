package com.medimitra.common.security;

import com.medimitra.common.core.UserId;
import java.util.UUID;

/** Port to Consent &amp; Access's {@code /access/check} (README section 6) — the single policy decision point every service defers to. */
public interface ConsentClient {
    boolean isAllowed(UserId actor, UUID patientId, String action);
}
