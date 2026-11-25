/**
 * Request-level idempotency for unsafe endpoints that carry an {@code Idempotency-Key} header
 * (e.g. booking an appointment), so a retried write is never applied twice (README section 10).
 */
package com.medimitra.common.web.idempotency;
