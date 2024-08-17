package io.gupshup.cams_scheduler.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.Duration;

@Entity
@Table(name = "webhook_events")
@IdClass(WebhookEvents.WebhookEventsId.class)
@Data
@NoArgsConstructor
public class WebhookEvents {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Id
    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "webhook_id", nullable = false)
    private String webhookId;

    @Column(name = "request_time", nullable = false)
    private Duration requestTime;

    @Column(name = "response_time", nullable = false)
    private Duration responseTime;

    @Column(name = "latency", nullable = false)
    private Duration latency;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error_msg")
    private String errorMsg;

    @Column(name = "payload")
    private String payload;

    // Composite Key Class
    @Data
    @NoArgsConstructor
    public static class WebhookEventsId implements Serializable {
        private String id;
        private OffsetDateTime timestamp;
    }
}

