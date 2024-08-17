package io.gupshup.cams_scheduler.services;

import io.gupshup.cams_scheduler.models.WebhookAnalytics;
import io.gupshup.cams_scheduler.models.WebhookEvents;
import io.gupshup.cams_scheduler.repositories.WebhookAnalyticsRepository;
import io.gupshup.cams_scheduler.repositories.WebhookEventsRepository;
import io.gupshup.cams_scheduler.repositories.WebhookHealthRepository;
import io.gupshup.cams_scheduler.utils.HealthScoreCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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
    private ZooKeeperService zooKeeperService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Number of threads for parallel processing

    @Transactional
    public void aggregate() {
        List<WebhookAnalytics> analyticsList = webhookAnalyticsRepository.findAll();

        analyticsList.forEach(analytics -> executorService.submit(() -> {
            String webhookId = analytics.getWebhookId();
            OffsetDateTime lastAggregationTime = analytics.getLastAggregationTimestamp();
            if (lastAggregationTime == null) {
                try {
                    lastAggregationTime = OffsetDateTime.now().minusMinutes(zooKeeperService.getAggregationIntervalMinutes()); // Default interval, update based on dynamic config if needed
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            List<WebhookEvents> events = webhookEventsRepository.findAllByWebhookIdAndTimestampAfter(webhookId, lastAggregationTime);
            WebhookAnalytics updatedAnalytics = updateAggregates(analytics, events);
            updatedAnalytics.setLastAggregationTimestamp(OffsetDateTime.now());
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
            updatedAnalytics.setLastAggregationTimestamp(OffsetDateTime.now());
            webhookAnalyticsRepository.save(updatedAnalytics);
        }));
    }

    private WebhookAnalytics resetAggregationData(WebhookAnalytics analytics) {
        // Create a new WebhookAnalytics object with reset values
        WebhookAnalytics resetAnalytics = new WebhookAnalytics();
        resetAnalytics.setWebhookId(analytics.getWebhookId());
        resetAnalytics.setSuccessRate(0.0);
        resetAnalytics.setAvgLatency(0.0);
        resetAnalytics.setRetryRate(0.0);
        resetAnalytics.setTotalEvents(0);
        resetAnalytics.setHealth(calculateHealthScore(analytics.getWebhookId()));
        return resetAnalytics;
    }

    private WebhookAnalytics updateAggregates(WebhookAnalytics existingAnalytics, List<WebhookEvents> newEvents) {
        double totalLatency = existingAnalytics.getAvgLatency() * existingAnalytics.getTotalEvents();
        int successfulRequests = (int) (existingAnalytics.getSuccessRate() / 100 * existingAnalytics.getTotalEvents());
        int totalRequests = existingAnalytics.getTotalEvents();
        int retryCount = (int) (existingAnalytics.getRetryRate() / 100 * totalRequests);

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
        double retryRate = totalRequests == 0 ? 0 : (double) retryCount / totalRequests * 100;

        existingAnalytics.setSuccessRate(successRate);
        existingAnalytics.setAvgLatency(avgLatency);
        existingAnalytics.setRetryRate(retryRate);
        existingAnalytics.setTotalEvents(totalRequests);
        existingAnalytics.setHealth(calculateHealthScore(existingAnalytics.getWebhookId()));

        return existingAnalytics;
    }

    private double calculateHealthScore(String webhookId) {
        double downtimeInMinutes = webhookHealthRepository.findDowntimeByWebhookIdAndDate(webhookId, OffsetDateTime.now().toLocalDate());
        double downtimeInSeconds = downtimeInMinutes * 60;

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfDay = now.minusDays(1).toLocalDate().atStartOfDay(now.getOffset()).toOffsetDateTime();

        int failedRequestsPastDay = webhookEventsRepository.countFailedRequestsPastDay(webhookId, startOfDay, now);
        int totalRequestsPastDay = webhookEventsRepository.countTotalRequestsPastDay(webhookId, startOfDay, now);

        return HealthScoreCalculator.calculateHealthScore(
                downtimeInSeconds, 86400, // 24 hours in seconds
                failedRequestsPastDay, totalRequestsPastDay,
                webhookAnalyticsRepository.findHealthScoreByWebhookId(webhookId)
        );
    }
}
