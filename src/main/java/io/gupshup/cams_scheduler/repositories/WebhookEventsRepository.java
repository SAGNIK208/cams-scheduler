package io.gupshup.cams_scheduler.repositories;

import io.gupshup.cams_scheduler.models.WebhookEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface WebhookEventsRepository extends JpaRepository<WebhookEvents, WebhookEvents.WebhookEventsId> {
    List<WebhookEvents> findAllByWebhookId(String webhookId);
    List<WebhookEvents> findAllByTimestampBetween(OffsetDateTime start, OffsetDateTime end);
    @Query("SELECT COUNT(*) FROM webhook_events WHERE webhook_id = :webhookId AND timestamp BETWEEN :startTime AND :endTime AND is_success = false")
    int countFailedRequestsPastDay(@Param("webhookId") String webhookId, @Param("startTime") OffsetDateTime startTime, @Param("endTime") OffsetDateTime endTime);

    @Query("SELECT COUNT(*) FROM webhook_events WHERE webhook_id = :webhookId AND timestamp BETWEEN :startTime AND :endTime")
    int countTotalRequestsPastDay(@Param("webhookId") String webhookId, @Param("startTime") OffsetDateTime startTime, @Param("endTime") OffsetDateTime endTime);

    @Query("SELECT e FROM WebhookEvents e WHERE e.webhookId = :webhookId AND e.timestamp > :lastAggregationTime")
    List<WebhookEvents> findAllByWebhookIdAndTimestampAfter(@Param("webhookId") String webhookId, @Param("lastAggregationTime") OffsetDateTime lastAggregationTime);
    // Custom query methods (if any) can be added here
}

