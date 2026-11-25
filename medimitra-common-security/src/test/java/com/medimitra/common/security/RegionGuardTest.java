package com.medimitra.common.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.medimitra.common.core.Region;
import com.medimitra.common.core.exception.RegionMismatchException;
import org.junit.jupiter.api.Test;

class RegionGuardTest {

    @Test
    void allowsMatchingRegion() {
        RegionGuard guard = new RegionGuard(Region.IN);
        assertDoesNotThrow(() -> guard.verify(Region.IN));
    }

    @Test
    void rejectsMismatchedRegion() {
        RegionGuard guard = new RegionGuard(Region.IN);
        assertThrows(RegionMismatchException.class, () -> guard.verify(Region.EU));
    }
}
