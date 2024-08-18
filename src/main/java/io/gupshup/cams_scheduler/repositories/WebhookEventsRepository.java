package io.gupshup.cams_scheduler.repositories;

import io.gupshup.cams_scheduler.models.WebhookEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface WebhookEventsRepository extends JpaRepository<WebhookEvents, WebhookEvents.WebhookEventsId> {
    List<WebhookEvents> findAllByWebhookId(String webhookId);
    List<WebhookEvents> findAllByTimestampBetween(OffsetDateTime start, OffsetDateTime end);
    @Query("SELECT COUNT(e) FROM WebhookEvents e WHERE e.webhookId = :webhookId AND e.isSuccess = false AND e.timestamp BETWEEN :startDate AND :endDate")
    long countFailedRequestsPastDay(@Param("webhookId") String webhookId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query("SELECT COUNT(e) FROM WebhookEvents e WHERE e.webhookId = :webhookId AND e.timestamp BETWEEN :startDate AND :endDate")
    long countTotalRequestsPastDay(@Param("webhookId") String webhookId, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query("SELECT e FROM WebhookEvents e WHERE e.webhookId = :webhookId AND e.timestamp > :lastAggregationTime")
    List<WebhookEvents> findAllByWebhookIdAndTimestampAfter(@Param("webhookId") String webhookId, @Param("lastAggregationTime") Timestamp lastAggregationTime);
    // Custom query methods (if any) can be added here
}

