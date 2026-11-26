package com.medimitra.identityaccess.application.port;

/** Delivers a one-time code to the user (SMS/email/WhatsApp — typically via the Notification service). */
public interface OtpSender {
    void send(String email, String code);
}
