package com.medimitra.common.security;

import com.medimitra.common.core.exception.AuthenticationFailedException;

/** Section 8.4: pay, grant/revoke consent, export data and delete records all require a fresh MFA acr. */
public final class StepUpGuard {

    private StepUpGuard() {
    }

    public static void require(AuthenticatedPrincipal principal, String requiredAcr) {
        if (!requiredAcr.equals(principal.acr())) {
            throw new AuthenticationFailedException(
                    SecurityErrorCodes.STEP_UP_REQUIRED, "this action requires step-up authentication (acr=" + requiredAcr + ")");
        }
    }
}
