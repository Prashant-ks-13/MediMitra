package com.medimitra.common.web.idempotency;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.exception.DomainValidationException;
import com.medimitra.common.core.exception.IdempotencyKeyReuseException;
import com.medimitra.common.web.WebErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

/** Backs {@link Idempotent}: replays a cached response for a repeated key+body, rejects a reused key with a different body. */
public class IdempotencyInterceptor implements HandlerInterceptor {

    public static final String HEADER = "Idempotency-Key";
    private static final String FINGERPRINT_ATTRIBUTE = IdempotencyInterceptor.class.getName() + ".fingerprint";
    private static final String KEY_ATTRIBUTE = IdempotencyInterceptor.class.getName() + ".key";

    private final IdempotencyStore store;
    private final ClockProvider clockProvider;

    public IdempotencyInterceptor(IdempotencyStore store, ClockProvider clockProvider) {
        this.store = store;
        this.clockProvider = clockProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod) || !handlerMethod.hasMethodAnnotation(Idempotent.class)) {
            return true;
        }
        String key = request.getHeader(HEADER);
        if (key == null || key.isBlank()) {
            throw new DomainValidationException(WebErrorCodes.IDEMPOTENCY_KEY_MISSING, "Idempotency-Key header is required for this request");
        }
        byte[] body = (request instanceof BufferedBodyHttpServletRequest buffered) ? buffered.body() : new byte[0];
        String fingerprint = fingerprint(request.getMethod(), request.getRequestURI(), body);

        Optional<IdempotencyRecord> existing = store.find(key);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (!record.fingerprint().equals(fingerprint)) {
                throw new IdempotencyKeyReuseException(
                        WebErrorCodes.IDEMPOTENCY_KEY_REUSED, "Idempotency-Key " + key + " was already used with a different request");
            }
            replay(response, record);
            return false;
        }

        request.setAttribute(KEY_ATTRIBUTE, key);
        request.setAttribute(FINGERPRINT_ATTRIBUTE, fingerprint);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String key = (String) request.getAttribute(KEY_ATTRIBUTE);
        if (key == null || ex != null) {
            return;
        }
        if (response.getStatus() >= 200 && response.getStatus() < 300 && response instanceof ContentCachingResponseWrapper wrapped) {
            String fingerprint = (String) request.getAttribute(FINGERPRINT_ATTRIBUTE);
            store.save(new IdempotencyRecord(
                    key, fingerprint, response.getStatus(), response.getContentType(), wrapped.getContentAsByteArray(), clockProvider.now()));
        }
    }

    private void replay(HttpServletResponse response, IdempotencyRecord record) throws IOException {
        response.setStatus(record.status());
        if (record.contentType() != null) {
            response.setContentType(record.contentType());
        }
        response.getOutputStream().write(record.body());
    }

    private static String fingerprint(String method, String uri, byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(method.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) ':');
            digest.update(uri.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) ':');
            digest.update(body);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
