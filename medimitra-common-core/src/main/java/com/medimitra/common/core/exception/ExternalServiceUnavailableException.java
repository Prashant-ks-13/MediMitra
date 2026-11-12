package com.medimitra.common.core.exception;

import com.medimitra.common.core.error.ErrorCode;

public class ExternalServiceUnavailableException extends IntegrationException {

    public ExternalServiceUnavailableException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, java.util.Map.of(), cause);
    }

    @Override
    public int httpStatus() {
        return 503;
    }
}
