package com.medimitra.common.security;

import com.medimitra.common.core.exception.ConsentRequiredException;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;

/** Backs {@link RequiresConsent}: calls {@link ConsentClient} before the annotated method runs. */
@Aspect
public class ConsentEnforcementAspect {

    private final ConsentClient consentClient;

    public ConsentEnforcementAspect(ConsentClient consentClient) {
        this.consentClient = consentClient;
    }

    @Around("@annotation(requiresConsent)")
    public Object enforce(ProceedingJoinPoint joinPoint, RequiresConsent requiresConsent) throws Throwable {
        AuthenticatedPrincipal principal = (AuthenticatedPrincipal) SecurityContextHolder.getContext().getAuthentication();
        UUID patientId = extractPatientId(joinPoint, requiresConsent.patientIdParam());
        if (!consentClient.isAllowed(principal.subject(), patientId, requiresConsent.action())) {
            throw new ConsentRequiredException(
                    SecurityErrorCodes.CONSENT_REQUIRED, "no consent for action " + requiresConsent.action() + " on patient " + patientId);
        }
        return joinPoint.proceed();
    }

    private static UUID extractPatientId(ProceedingJoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] names = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(paramName)) {
                return (UUID) args[i];
            }
        }
        throw new IllegalStateException("no parameter named " + paramName + " on " + signature);
    }
}
