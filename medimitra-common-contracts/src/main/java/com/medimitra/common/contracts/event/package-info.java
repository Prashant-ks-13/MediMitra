/**
 * Versioned event payloads published on the Kafka topics catalogued in the HLD/LLD (section 6).
 *
 * <p>Each payload is a plain, framework-free Java record named {@code <Aggregate><Action>V1}. A
 * breaking change to a payload means adding a {@code V2} record and publishing it alongside
 * {@code V1} until every consumer has moved, per the common-module governance rules; existing
 * {@code V1} records are never changed in place.
 *
 * <p>These records carry only identifiers, classification fields and timestamps, not full entity
 * state: a consumer that needs more fetches it from the owning service's API. This keeps services
 * decoupled and avoids turning Kafka into a second, inconsistent copy of each service's database.
 */
package com.medimitra.common.contracts.event;
