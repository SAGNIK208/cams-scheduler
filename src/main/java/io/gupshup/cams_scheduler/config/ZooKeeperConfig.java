package io.gupshup.cams_scheduler.config;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.WatchedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ZooKeeperConfig {

    @Value("${zookeeper.hosts}")
    private String zookeeperHosts;

    @Value("${zookeeper.session.timeout}")
    private int sessionTimeout;

    @Bean
    public ZooKeeper zooKeeper() throws IOException {
        return new ZooKeeper(zookeeperHosts, sessionTimeout, new DefaultWatcher());
    }

    private static class DefaultWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            // Handle ZooKeeper events here
            System.out.println("WatchedEvent: " + event);
        }
    }
}
