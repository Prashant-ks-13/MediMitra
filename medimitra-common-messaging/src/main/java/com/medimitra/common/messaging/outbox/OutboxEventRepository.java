package com.medimitra.common.messaging.outbox;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Locks the next batch of pending rows so that multiple relay instances across replicas never
     * dispatch the same event twice. {@code SKIP LOCKED} is PostgreSQL syntax, matching every
     * service's primary store.
     */
    @Query(
            value = """
            select * from outbox_event
            where status = 'PENDING'
            order by occurred_at
            limit :limit
            for update skip locked
            """,
            nativeQuery = true)
    List<OutboxEvent> lockNextBatch(@Param("limit") int limit);
}
