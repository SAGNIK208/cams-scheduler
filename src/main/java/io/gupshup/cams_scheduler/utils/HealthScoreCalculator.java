package io.gupshup.cams_scheduler.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class HealthScoreCalculator {

    // Define weightages for each parameter
    private static final double WEIGHTAGE_EXISTING_SCORE = 0.4;
    private static final double WEIGHTAGE_DOWNTIME_PERCENTAGE = 0.3;
    private static final double WEIGHTAGE_FAILURE_RATE = 0.3;

    public static double calculateHealthScore(double existingScore, long failedRequests, long totalRequests, double downtime, double totalTime) {
        try{
            if (totalRequests < 0 || totalTime < 0 || existingScore < 0 || existingScore > 100) {
                throw new IllegalArgumentException("Invalid input values.");
            }

            if(totalRequests == 0 || totalTime == 0){
                return existingScore;
            }

            double failureRate = (double) failedRequests / totalRequests;
            double downtimePercentage = downtime / totalTime;

            double normalizedFailureRate = Math.max(0, Math.min(100, 100 * (1 - failureRate))); // Lower failure rate is better
            double normalizedDowntimePercentage = Math.max(0, Math.min(100, 100 * (1 - downtimePercentage))); // Lower downtime is better

            double healthScore = (WEIGHTAGE_EXISTING_SCORE * existingScore +
                    WEIGHTAGE_FAILURE_RATE * normalizedFailureRate +
                    WEIGHTAGE_DOWNTIME_PERCENTAGE * normalizedDowntimePercentage);

            return Math.max(0, Math.min(100, healthScore));
        } catch (Exception e) {
            return existingScore;
        }
    }

    public static void main(String[] args) {

        // Example inputs
        double existingScore = 0.0;
        int failedRequests = 0;
        int totalRequests = 100;
        double downtime = 2.0;
        double totalTime = 24.0;
        System.out.println(LocalDateTime.now(ZoneOffset.UTC).toLocalTime());
        double healthScore = calculateHealthScore(existingScore, failedRequests, totalRequests, downtime, totalTime);
        System.out.println("Computed Health Score: " + healthScore);
    }
}