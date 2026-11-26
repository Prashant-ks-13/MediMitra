package com.medimitra.identityaccess.infrastructure.otp;

import com.medimitra.identityaccess.application.OtpRecord;
import com.medimitra.identityaccess.application.OtpStore;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Single-instance default; a multi-instance deployment needs a shared (Redis) implementation instead. */
public final class InMemoryOtpStore implements OtpStore {

    private final Map<String, OtpRecord> records = new ConcurrentHashMap<>();

    @Override
    public void save(String email, OtpRecord record) {
        records.put(email, record);
    }

    @Override
    public Optional<OtpRecord> find(String email) {
        return Optional.ofNullable(records.get(email));
    }

    @Override
    public void invalidate(String email) {
        records.remove(email);
    }
}
