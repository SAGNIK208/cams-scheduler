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

    private ZooKeeperConfig(@Value("${zookeeper.connection.string}") String zkConnectionString) throws Exception {
        this.zkConnectionString = zkConnectionString;
        CountDownLatch connectedSignal = new CountDownLatch(1);
        this.zooKeeper = new ZooKeeper(zkConnectionString, 3000, this);
        connectedSignal.await(3, TimeUnit.SECONDS);
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
                setupWatchers(); // Re-register watchers
                System.out.println("Configuration changed. Path: " + event.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getConfigValue() throws Exception {
        byte[] data = zooKeeper.getData(CONFIG_PATH, false, null);
        return new String(data, StandardCharsets.UTF_8);
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

    public int getAggregationIntervalMinutes() throws Exception {
        JSONObject configJson = getConfigJson();
        return configJson.getInt("aggregationIntervalMinutes");
    }

    public boolean isSchedulerRunning() throws Exception {
        JSONObject configJson = getConfigJson();
        return configJson.getBoolean("isRunning");
    }

    public int getNumberOfThreads() throws Exception {
        JSONObject configJson = getConfigJson();
        return configJson.getInt("numberOfThreads");
    }
}
