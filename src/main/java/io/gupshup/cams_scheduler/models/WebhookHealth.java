package io.gupshup.cams_scheduler.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "webhook_health")
@IdClass(WebhookHealth.WebhookHealthId.class)
@Data
@NoArgsConstructor
public class WebhookHealth {

    @Id
    @Column(name = "webhook_id", nullable = false)
    private String webhookId;

    @Id
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "downtime", precision = 10)
    private Double downtime;

    // Composite Key Class
    @Data
    @NoArgsConstructor
    public static class WebhookHealthId implements Serializable {
        private String webhookId;
        private LocalDate date;
    }
}
