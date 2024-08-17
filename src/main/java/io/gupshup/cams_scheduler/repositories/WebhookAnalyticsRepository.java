package io.gupshup.cams_scheduler.repositories;

import io.gupshup.cams_scheduler.models.WebhookAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookAnalyticsRepository extends JpaRepository<WebhookAnalytics, String> {
    @Query("SELECT a.health FROM WebhookAnalytics a WHERE a.webhookId = :webhookId")
    Double findHealthScoreByWebhookId(@Param("webhookId") String webhookId);
    // Custom query methods (if any) can be added here
}

