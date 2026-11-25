package com.medimitra.common.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.medimitra.common.core.RandomIdGenerator;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    @Test
    void mintsAndEchoesACorrelationIdWhenNoneSupplied() throws ServletException, IOException {
        CorrelationIdFilter filter = new CorrelationIdFilter(new RandomIdGenerator());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/patients/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                assertNotNull(MDC.get(CorrelationIdHolder.MDC_KEY));
            }
        };

        filter.doFilter(request, response, chain);

        assertNotNull(response.getHeader(CorrelationIdFilter.HEADER));
        assertNull(MDC.get(CorrelationIdHolder.MDC_KEY));
    }

    @Test
    void reusesAnInboundCorrelationId() throws ServletException, IOException {
        CorrelationIdFilter filter = new CorrelationIdFilter(new RandomIdGenerator());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/patients/1");
        request.addHeader(CorrelationIdFilter.HEADER, "corr-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("corr-123", response.getHeader(CorrelationIdFilter.HEADER));
    }
}
