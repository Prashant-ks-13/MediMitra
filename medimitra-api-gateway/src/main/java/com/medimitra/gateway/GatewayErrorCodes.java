package com.medimitra.gateway;

import com.medimitra.common.core.error.ErrorCode;

/** Stable codes raised by the gateway itself, under the {@code GW} service segment. */
public final class GatewayErrorCodes {

    private GatewayErrorCodes() {
    }

    public static final ErrorCode RATE_LIMIT_EXCEEDED = new ErrorCode("MM-GW-429-RATE_LIMIT_EXCEEDED");
}
