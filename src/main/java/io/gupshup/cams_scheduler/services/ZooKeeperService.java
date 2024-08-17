package io.gupshup.cams_scheduler.services;

import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class ZooKeeperService implements Watcher {

    private final ZooKeeper zooKeeper;
    private final String zkConnectionString;
    private static final String CONFIG_PATH = "/cams/config";

    public ZooKeeperService(@Value("${zookeeper.connection.string}") String zkConnectionString) throws Exception {
        this.zkConnectionString = zkConnectionString;
        CountDownLatch connectedSignal = new CountDownLatch(1);
        this.zooKeeper = new ZooKeeper(zkConnectionString, 3000, this);
        connectedSignal.await(3, TimeUnit.SECONDS);
        setupWatchers();
    }

    private void setupWatchers() throws KeeperException, InterruptedException {
        zooKeeper.exists(CONFIG_PATH + "/aggregationIntervalMinutes", true);
        zooKeeper.exists(CONFIG_PATH + "/isRunning", true);
        zooKeeper.exists(CONFIG_PATH + "/numberOfThreads", true);
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            // Handle node change events
            if (event.getType() == Event.EventType.NodeDataChanged) {
                setupWatchers(); // Re-register watches
                System.out.println("Configuration changed. Path: " + event.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getAggregationIntervalMinutes() throws Exception {
        byte[] data = zooKeeper.getData(CONFIG_PATH + "/aggregationIntervalMinutes", false, null);
        return Integer.parseInt(new String(data, StandardCharsets.UTF_8));
    }

    public boolean isSchedulerRunning() throws Exception {
        byte[] data = zooKeeper.getData(CONFIG_PATH + "/isRunning", false, null);
        return Boolean.parseBoolean(new String(data, StandardCharsets.UTF_8));
    }

    public int getNumberOfThreads() throws Exception {
        byte[] data = zooKeeper.getData(CONFIG_PATH + "/numberOfThreads", false, null);
        return Integer.parseInt(new String(data, StandardCharsets.UTF_8));
    }
}
