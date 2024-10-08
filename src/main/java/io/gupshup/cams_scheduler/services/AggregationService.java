package io.gupshup.cams_scheduler.services;

import io.gupshup.cams_scheduler.config.ZooKeeperConfig;
import io.gupshup.cams_scheduler.models.WebhookAnalytics;
import io.gupshup.cams_scheduler.models.WebhookEvents;
import io.gupshup.cams_scheduler.repositories.WebhookAnalyticsRepository;
import io.gupshup.cams_scheduler.repositories.WebhookEventsRepository;
import io.gupshup.cams_scheduler.repositories.WebhookHealthRepository;
import io.gupshup.cams_scheduler.utils.HealthScoreCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AggregationService {

    @Autowired
    private WebhookHealthRepository webhookHealthRepository;

    @Autowired
    private WebhookEventsRepository webhookEventsRepository;

    @Autowired
    private WebhookAnalyticsRepository webhookAnalyticsRepository;

    @Autowired
    private ZooKeeperConfig zooKeeperConfig;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1); // Number of threads for parallel processing

    @Transactional
    public void aggregate() {
        List<WebhookAnalytics> analyticsList = webhookAnalyticsRepository.findAll();

        analyticsList.forEach(analytics -> executorService.submit(() -> {
            String webhookId = analytics.getWebhookId();
            Timestamp lastAggregationTime = analytics.getLastAggregationTimestamp();
            if (lastAggregationTime == null) {
                try {
                    lastAggregationTime = Timestamp.valueOf(
                            LocalDateTime.now(ZoneOffset.UTC)
                                    .minusDays(31));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            List<WebhookEvents> events = webhookEventsRepository.findAllByWebhookIdAndTimestampAfter(webhookId, lastAggregationTime);
            WebhookAnalytics updatedAnalytics = updateAggregates(analytics, events);
            updatedAnalytics.setLastAggregationTimestamp(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
            webhookAnalyticsRepository.save(updatedAnalytics);
        }));
    }

    @Transactional
    public void triggerFullAggregation() {
        List<WebhookAnalytics> analyticsList = webhookAnalyticsRepository.findAll();

        analyticsList.forEach(analytics -> executorService.submit(() -> {
            String webhookId = analytics.getWebhookId();

            // Fetch all events without considering the last aggregation timestamp
            List<WebhookEvents> allEvents = webhookEventsRepository.findAllByWebhookId(webhookId);

            // Clear existing aggregated data for this webhook
            WebhookAnalytics updatedAnalytics = resetAggregationData(analytics);

            // Perform aggregation on all events
            updateAggregates(updatedAnalytics, allEvents);
            updatedAnalytics.setLastAggregationTimestamp(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
            webhookAnalyticsRepository.save(updatedAnalytics);
        }));
    }

    private WebhookAnalytics resetAggregationData(WebhookAnalytics analytics) {
        // Create a new WebhookAnalytics object with reset values
        WebhookAnalytics resetAnalytics = new WebhookAnalytics();
        resetAnalytics.setWebhookId(analytics.getWebhookId());
        resetAnalytics.setSuccessRate(0.0);
        resetAnalytics.setAvgLatency(0.0);
        resetAnalytics.setRetryCount(0);
        resetAnalytics.setTotalEvents(0);
        resetAnalytics.setHealth(calculateHealthScore(analytics.getWebhookId()));
        return resetAnalytics;
    }

    private WebhookAnalytics updateAggregates(WebhookAnalytics existingAnalytics, List<WebhookEvents> newEvents) {
        double totalLatency = existingAnalytics.getAvgLatency() * existingAnalytics.getTotalEvents();
        int successfulRequests = (int) (existingAnalytics.getSuccessRate() / 100 * existingAnalytics.getTotalEvents());
        int totalRequests = existingAnalytics.getTotalEvents();
        int retryCount = existingAnalytics.getRetryCount();

        for (WebhookEvents event : newEvents) {
            if (event.getIsSuccess()) {
                successfulRequests++;
            }
            totalRequests++;
            retryCount += event.getRetryCount();
            totalLatency += event.getLatency().toMillis();
        }

        double successRate = totalRequests == 0 ? 0 : (double) successfulRequests / totalRequests * 100;
        double avgLatency = totalRequests == 0 ? 0 : totalLatency / totalRequests;

        existingAnalytics.setSuccessRate(successRate);
        existingAnalytics.setAvgLatency(avgLatency);
        existingAnalytics.setRetryCount(retryCount);
        existingAnalytics.setTotalEvents(totalRequests);
        existingAnalytics.setHealth(calculateHealthScore(existingAnalytics.getWebhookId()));

        return existingAnalytics;
    }

    private double calculateHealthScore(String webhookId) {
        double downtimeInMinutes = webhookHealthRepository.findDowntimeByWebhookIdAndDate(webhookId, OffsetDateTime.now().toLocalDate());
        double downtimeInSeconds = downtimeInMinutes * 60;

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfDay = now.minusDays(1).toLocalDate().atStartOfDay(now.getOffset()).toOffsetDateTime();

        long failedRequestsPastDay = webhookEventsRepository.countFailedRequestsPastDay(webhookId, Timestamp.from(startOfDay.toInstant()), Timestamp.from(now.toInstant()));
        long totalRequestsPastDay = webhookEventsRepository.countTotalRequestsPastDay(webhookId, Timestamp.from(startOfDay.toInstant()), Timestamp.from(now.toInstant()));

        return HealthScoreCalculator.calculateHealthScore(
                webhookAnalyticsRepository.findHealthScoreByWebhookId(webhookId),
                failedRequestsPastDay, totalRequestsPastDay,
                downtimeInSeconds, LocalDateTime.now(ZoneOffset.UTC).toLocalTime().toSecondOfDay()
        );
    }
}
