package com.medimitra.identityaccess.infrastructure.otp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.medimitra.identityaccess.application.port.OtpRecord;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class InMemoryOtpStoreTest {

    @Test
    void savesFindsAndInvalidates() {
        InMemoryOtpStore store = new InMemoryOtpStore();
        store.save("a@b.com", new OtpRecord("hash-1", Instant.now().plusSeconds(60)));

        assertTrue(store.find("a@b.com").isPresent());

        store.invalidate("a@b.com");

        assertFalse(store.find("a@b.com").isPresent());
    }
}
