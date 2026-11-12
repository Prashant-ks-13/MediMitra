package com.medimitra.common.core;

import java.util.UUID;

/** Marker for identifiers wrapping a UUID, so a doctor id can never be passed where a patient id is expected. */
public interface TypedId {
    UUID value();
}
