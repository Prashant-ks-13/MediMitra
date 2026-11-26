package com.medimitra.identityaccess.infrastructure.otp;

import com.medimitra.identityaccess.application.port.OtpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Development-only default: logs the OTP instead of sending it anywhere. A real deployment MUST
 * replace this with a bean that actually delivers the code (e.g. via the Notification service) —
 * never ship this to production, since it writes a live auth credential straight into the logs.
 */
public final class LoggingOtpSender implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingOtpSender.class);

    @Override
    public void send(String email, String code) {
        log.warn("DEV ONLY — no real OTP channel configured; code for {} is {}", email, code);
    }
}
