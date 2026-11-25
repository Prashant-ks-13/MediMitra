package com.medimitra.common.security;

import com.medimitra.common.core.Region;
import com.medimitra.common.core.exception.RegionMismatchException;

/** Rejects a request whose token region does not match this cell's region (section 5.1). */
public class RegionGuard {

    private final Region homeRegion;

    public RegionGuard(Region homeRegion) {
        this.homeRegion = homeRegion;
    }

    public void verify(Region tokenRegion) {
        if (tokenRegion != homeRegion) {
            throw new RegionMismatchException(
                    SecurityErrorCodes.REGION_MISMATCH,
                    "token region " + tokenRegion + " does not match this cell's region " + homeRegion);
        }
    }
}
