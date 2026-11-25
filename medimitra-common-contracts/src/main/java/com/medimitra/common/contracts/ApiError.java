package com.medimitra.common.contracts;

import java.time.Instant;

/** RFC 9457 problem details, extended with our stable error code and the request's correlation id. */
public record ApiError(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String code,
        String correlationId,
        Instant timestamp) {

    public static ApiError of(String code, int status, String title, String detail, String instance, String correlationId, Instant timestamp) {
        return new ApiError("https://medimitra.dev/errors/" + code, title, status, detail, instance, code, correlationId, timestamp);
    }
}
