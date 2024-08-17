package io.gupshup.cams_scheduler.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerService {

    @Autowired
    private ZooKeeperService zooKeeperService;

    @Autowired
    private AggregationService aggregationService;

    private ScheduledExecutorService scheduler;
    private int aggregationIntervalMinutes = 5; // Default value
    private boolean isRunning = true; // Default value
    private int numberOfThreads = 1; // Default value

    @PostConstruct
    public void init() {
        startDynamicScheduler();
    }

    private void startDynamicScheduler() {
        scheduler = Executors.newScheduledThreadPool(numberOfThreads);
        scheduler.scheduleAtFixedRate(() -> {
            if (isRunning) {
                aggregationService.aggregate();
            }
        }, 0, aggregationIntervalMinutes, TimeUnit.MINUTES);
    }

    public void updateConfiguration() {
        try {
            int newInterval = zooKeeperService.getAggregationIntervalMinutes();
            boolean newRunningStatus = zooKeeperService.isSchedulerRunning();
            int newThreadCount = zooKeeperService.getNumberOfThreads();

            if (newInterval != aggregationIntervalMinutes || newRunningStatus != isRunning || newThreadCount != numberOfThreads) {
                aggregationIntervalMinutes = newInterval;
                isRunning = newRunningStatus;
                numberOfThreads = newThreadCount;

                rescheduleTasks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rescheduleTasks() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        startDynamicScheduler();
    }
}
