package io.gupshup.cams_scheduler.config;

import org.apache.zookeeper.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class ZooKeeperConfig implements Watcher {

    private static ZooKeeperConfig instance;

    private final ZooKeeper zooKeeper;
    private final String zkConnectionString;
    private static final String CONFIG_PATH = "/cams/config";
    private JSONObject configJson = new JSONObject();

    private ZooKeeperConfig(@Value("${zookeeper.connection.string}") String zkConnectionString) throws Exception {
        this.zkConnectionString = zkConnectionString;
        CountDownLatch connectedSignal = new CountDownLatch(1);
        this.zooKeeper = new ZooKeeper(zkConnectionString, 30000000, this);
        connectedSignal.await(3, TimeUnit.SECONDS);
        loadConfig();
        setupWatchers();
    }

    private void loadConfig() throws KeeperException, InterruptedException {
        try {
            byte[] data = zooKeeper.getData(CONFIG_PATH, this, null);
            this.configJson = new JSONObject(new String(data, StandardCharsets.UTF_8));
            System.out.println("Configuration loaded: " + this.configJson.toString());
        } catch (Exception e) {
            System.err.println("Failed to fetch configuration: " + e.getMessage());
        }
    }

    public static synchronized ZooKeeperConfig getInstance(String zkConnectionString) throws Exception {
        if (instance == null) {
            instance = new ZooKeeperConfig(zkConnectionString);
        }
        return instance;
    }

    private void setupWatchers() throws KeeperException, InterruptedException {
        zooKeeper.exists(CONFIG_PATH, true);
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            if (event.getType() == Event.EventType.NodeDataChanged) {
                loadConfig();
                setupWatchers(); // Re-register watchers
                System.out.println("Configuration changed. Path: " + event.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getConfigValue() throws Exception {
       return configJson.toString();
    }

    public void setConfigValue(String jsonData) throws Exception {
        if (zooKeeper.exists(CONFIG_PATH, false) != null) {
            zooKeeper.setData(CONFIG_PATH, jsonData.getBytes(StandardCharsets.UTF_8), -1);
        } else {
            zooKeeper.create(CONFIG_PATH, jsonData.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }


    public JSONObject getConfigJson() throws Exception {
        String configJsonStr = getConfigValue();
        return new JSONObject(configJsonStr);
    }

    public int getAggregationIntervalMinutes() {
        return this.configJson.optInt("aggregationIntervalMinutes", 5); // Default to 5 if not found
    }

    public boolean isSchedulerRunning() {
        return this.configJson.optBoolean("isRunning", false); // Default to false if not found
    }

    public int getNumberOfThreads() {
        return this.configJson.optInt("numberOfThreads", 3); // Default to 3 if not found
    }
}
