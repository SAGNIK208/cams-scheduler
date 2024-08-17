package io.gupshup.cams_scheduler.repositories;

import io.gupshup.cams_scheduler.models.WebhookHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface WebhookHealthRepository extends JpaRepository<WebhookHealth, WebhookHealth.WebhookHealthId> {
    @Query("SELECT h.downtime FROM WebhookHealth h WHERE h.webhook_id = :webhookId AND h.date = :date")
    double findDowntimeByWebhookIdAndDate(@Param("webhookId") String webhookId, @Param("date") LocalDate date);

    // Custom query methods (if any) can be added here
}

