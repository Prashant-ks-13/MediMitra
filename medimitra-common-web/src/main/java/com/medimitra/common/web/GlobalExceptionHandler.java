package com.medimitra.common.web;

import com.medimitra.common.contracts.ApiError;
import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.exception.MediMitraException;
import com.medimitra.common.core.exception.SecurityViolationException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps every {@link MediMitraException} (and anything unhandled) to an {@link ApiError} carrying
 * the request's correlation id (section 7.3). Security exceptions get a generic client-facing
 * message; the real detail goes only to logs.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ClockProvider clockProvider;

    public GlobalExceptionHandler(ClockProvider clockProvider) {
        this.clockProvider = clockProvider;
    }

    @ExceptionHandler(SecurityViolationException.class)
    public ResponseEntity<ApiError> handleSecurityViolation(SecurityViolationException e, HttpServletRequest request) {
        log.warn("security violation: code={} detail={}", e.errorCode(), e.getMessage());
        String genericMessage = e.httpStatus() == 401 ? "Authentication failed" : "Access denied";
        return respond(e.errorCode().value(), e.httpStatus(), e.getClass().getSimpleName(), genericMessage, request);
    }

    @ExceptionHandler(MediMitraException.class)
    public ResponseEntity<ApiError> handleMediMitraException(MediMitraException e, HttpServletRequest request) {
        if (e.httpStatus() >= 500) {
            log.error("business/integration failure: code={}", e.errorCode(), e);
        } else {
            log.info("request rejected: code={} detail={}", e.errorCode(), e.getMessage());
        }
        return respond(e.errorCode().value(), e.httpStatus(), e.getClass().getSimpleName(), e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage() == null ? "invalid" : fieldError.getDefaultMessage());
        }
        log.info("validation failed: {}", fieldErrors);
        return respond(WebErrorCodes.VALIDATION_FAILED.value(), 400, "DomainValidationException", "request failed validation", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("unhandled exception", e);
        return respond(WebErrorCodes.INTERNAL_ERROR.value(), 500, "InternalServerError", "an unexpected error occurred", request);
    }

    private ResponseEntity<ApiError> respond(String code, int status, String title, String detail, HttpServletRequest request) {
        ApiError apiError =
                ApiError.of(code, status, title, detail, request.getRequestURI(), CorrelationIdHolder.current(), clockProvider.now());
        return ResponseEntity.status(status).body(apiError);
    }
}
