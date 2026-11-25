/**
 * {@link com.medimitra.common.web.GlobalExceptionHandler}, correlation-id propagation, PII-free
 * request logging and idempotency ({@link com.medimitra.common.web.idempotency}) shared by every
 * REST service. Opt in with {@link com.medimitra.common.web.EnableMediMitraWeb}; nothing here is
 * a hidden bean.
 */
package com.medimitra.common.web;
