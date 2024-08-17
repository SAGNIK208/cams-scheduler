package io.gupshup.cams_scheduler.utils;

public class HealthScoreCalculator {

    private static final double DOWNTIME_WEIGHT = 0.5;
    private static final double HISTORICAL_WEIGHT = 0.5;
    private static final double FINAL_WEIGHT = 0.5;
    private static final int ROUNDING_MULTIPLE = 5;

    public static double calculateHealthScore(double downtimeInSeconds,
                                              double totalRequestDuration,
                                              long failedRequestsPastDay,
                                              long totalRequestsPastDay,
                                              Double existingHealthScore) {

        double downtimeImpact = calculateDowntimeImpact(downtimeInSeconds, totalRequestDuration);
        double historicalSuccessRate = calculateHistoricalSuccessRate(failedRequestsPastDay, totalRequestsPastDay);

        double newHealthScore = (DOWNTIME_WEIGHT * (1 - downtimeImpact)) + (HISTORICAL_WEIGHT * historicalSuccessRate);

        if (existingHealthScore != null) {
            newHealthScore = (FINAL_WEIGHT * newHealthScore) + ((1 - FINAL_WEIGHT) * existingHealthScore);
        }

        double scaledScore = newHealthScore * 100;
        return roundToNearestMultipleOf5(scaledScore);
    }

    private static double calculateDowntimeImpact(double downtimeInSeconds, double totalRequestDuration) {
        if (totalRequestDuration == 0) {
            return 0.0;
        }
        return downtimeInSeconds / totalRequestDuration;
    }

    private static double calculateHistoricalSuccessRate(long failedRequests, long totalRequests) {
        if (totalRequests == 0) {
            return 1.0;
        }
        return (double) (totalRequests - failedRequests) / totalRequests;
    }

    private static double roundToNearestMultipleOf5(double score) {
        return Math.round(score / ROUNDING_MULTIPLE) * ROUNDING_MULTIPLE;
    }

    public static void main(String[] args) {
        // Example usage
        double downtimeInSeconds = 300;
        double totalRequestDuration = 86400; // 24 hours in seconds
        int failedRequestsPastDay = 50;
        int totalRequestsPastDay = 1000;
        Double existingHealthScore = 0.85;

        double healthScore = calculateHealthScore(downtimeInSeconds, totalRequestDuration,
                failedRequestsPastDay, totalRequestsPastDay,
                existingHealthScore);

        System.out.printf("Health Score: %.2f%n", healthScore);
    }
}
