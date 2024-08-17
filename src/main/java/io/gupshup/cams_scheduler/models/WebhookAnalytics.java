package io.gupshup.cams_scheduler.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "webhook_analytics")
@Data
@NoArgsConstructor
public class WebhookAnalytics {

    @Id
    @Column(name = "webhook_id", nullable = false)
    private String webhookId;

    @Column(name = "success_rate", precision = 5)
    private Double successRate;

    @Column(name = "avg_latency", precision = 10)
    private Double avgLatency;

    @Column(name = "retry_rate", precision = 5)
    private Double retryRate;

    @Column(name = "total_events")
    private Integer totalEvents;

    @Column(name = "health", precision = 6)
    private Double health;

    @Column(name = "last_aggregation_time")
    private OffsetDateTime lastAggregationTimestamp;
}
