package com.medimitra.common.web.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.medimitra.common.core.SystemClockProvider;
import com.medimitra.common.core.exception.DomainValidationException;
import com.medimitra.common.core.exception.IdempotencyKeyReuseException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.ContentCachingResponseWrapper;

class IdempotencyInterceptorTest {

    static class DummyController {
        @Idempotent
        public void book() {
        }

        public void notIdempotent() {
        }
    }

    private HandlerMethod handlerMethodFor(String name) throws NoSuchMethodException {
        return new HandlerMethod(new DummyController(), DummyController.class.getMethod(name));
    }

    private IdempotencyInterceptor interceptor(InMemoryIdempotencyStore store) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new IdempotencyInterceptor(store, new SystemClockProvider(Clock.fixed(now, ZoneOffset.UTC)));
    }

    private BufferedBodyHttpServletRequest bufferedRequest(String body) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/appointments");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        request.addHeader(IdempotencyInterceptor.HEADER, "idem-key-1");
        return new BufferedBodyHttpServletRequest(request);
    }

    @Test
    void skipsEndpointsWithoutTheAnnotation() throws Exception {
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        IdempotencyInterceptor interceptor = interceptor(store);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, handlerMethodFor("notIdempotent")));
    }

    @Test
    void requiresTheIdempotencyKeyHeader() throws Exception {
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        IdempotencyInterceptor interceptor = interceptor(store);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/appointments");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(DomainValidationException.class, () -> interceptor.preHandle(request, response, handlerMethodFor("book")));
    }

    @Test
    void firstCallProceedsThenStoresTheResponseForReplay() throws Exception {
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        IdempotencyInterceptor interceptor = interceptor(store);
        BufferedBodyHttpServletRequest request = bufferedRequest("{\"slotId\":\"abc\"}");
        ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(new MockHttpServletResponse());
        HandlerMethod handler = handlerMethodFor("book");

        assertTrue(interceptor.preHandle(request, response, handler));

        response.setStatus(201);
        response.getWriter().write("{\"appointmentId\":\"e1\"}");
        response.getWriter().flush();

        interceptor.afterCompletion(request, response, handler, null);

        assertTrue(store.find("idem-key-1").isPresent());
        assertEquals(201, store.find("idem-key-1").get().status());
    }

    @Test
    void secondCallWithSameKeyAndBodyReplaysWithoutRunningTheHandler() throws Exception {
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        store.save(new IdempotencyRecord(
                "idem-key-1",
                fingerprintFor("POST", "/api/v1/appointments", "{\"slotId\":\"abc\"}"),
                201,
                "application/json",
                "{\"appointmentId\":\"e1\"}".getBytes(StandardCharsets.UTF_8),
                Instant.now()));
        IdempotencyInterceptor interceptor = interceptor(store);
        BufferedBodyHttpServletRequest request = bufferedRequest("{\"slotId\":\"abc\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(request, response, handlerMethodFor("book"));

        assertFalse(proceed);
        assertEquals(201, response.getStatus());
        assertEquals("{\"appointmentId\":\"e1\"}", response.getContentAsString());
    }

    @Test
    void sameKeyWithDifferentBodyIsRejected() throws Exception {
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        store.save(new IdempotencyRecord(
                "idem-key-1",
                fingerprintFor("POST", "/api/v1/appointments", "{\"slotId\":\"abc\"}"),
                201,
                "application/json",
                "{}".getBytes(StandardCharsets.UTF_8),
                Instant.now()));
        IdempotencyInterceptor interceptor = interceptor(store);
        BufferedBodyHttpServletRequest request = bufferedRequest("{\"slotId\":\"different\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(IdempotencyKeyReuseException.class, () -> interceptor.preHandle(request, response, handlerMethodFor("book")));
    }

    private static String fingerprintFor(String method, String uri, String body) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        digest.update(method.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) ':');
        digest.update(uri.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) ':');
        digest.update(body.getBytes(StandardCharsets.UTF_8));
        return java.util.HexFormat.of().formatHex(digest.digest());
    }
}
