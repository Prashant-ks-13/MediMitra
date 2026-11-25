package com.medimitra.common.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.medimitra.common.contracts.ApiError;
import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.core.error.ErrorCode;
import com.medimitra.common.core.exception.ResourceNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    @Test
    void mapsMediMitraExceptionToApiErrorWithCorrelationId() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        GlobalExceptionHandler handler = new GlobalExceptionHandler(new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC)));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/patients/1");

        try {
            org.slf4j.MDC.put(CorrelationIdHolder.MDC_KEY, "corr-1");
            ResourceNotFoundException exception =
                    new ResourceNotFoundException(new ErrorCode("MM-PAT-404-PATIENT_NOT_FOUND"), "patient not found");

            ResponseEntity<ApiError> response = handler.handleMediMitraException(exception, request);

            assertEquals(404, response.getStatusCode().value());
            assertEquals("MM-PAT-404-PATIENT_NOT_FOUND", response.getBody().code());
            assertEquals("corr-1", response.getBody().correlationId());
        } finally {
            org.slf4j.MDC.remove(CorrelationIdHolder.MDC_KEY);
        }
    }
}
