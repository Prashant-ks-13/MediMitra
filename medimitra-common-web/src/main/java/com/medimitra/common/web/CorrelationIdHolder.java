package com.medimitra.common.web;

import org.slf4j.MDC;

/** Reads the correlation id {@link CorrelationIdFilter} placed in MDC for the current request. */
public final class CorrelationIdHolder {

    public static final String MDC_KEY = "correlationId";

    private CorrelationIdHolder() {
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }
}
