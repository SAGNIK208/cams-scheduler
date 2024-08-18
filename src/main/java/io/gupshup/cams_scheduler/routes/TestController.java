package io.gupshup.cams_scheduler.routes;

import io.gupshup.cams_scheduler.config.ZooKeeperConfig;
import io.gupshup.cams_scheduler.models.WebhookEvents;
import io.gupshup.cams_scheduler.repositories.WebhookEventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ZooKeeperConfig zooKeeperConfig;
    @Autowired
    private WebhookEventsRepository webhookEventsRepository;

    @GetMapping("/zk-check")
    public ResponseEntity<String> checkZooKeeperValues() {
        try {
            int aggregationInterval = zooKeeperConfig.getAggregationIntervalMinutes();
            boolean isRunning = zooKeeperConfig.isSchedulerRunning();
            int numberOfThreads = zooKeeperConfig.getNumberOfThreads();

            String responseMessage = String.format(
                    "ZooKeeper Values - aggregationIntervalMinutes: %d, isRunning: %b, numberOfThreads: %d",
                    aggregationInterval, isRunning, numberOfThreads
            );
            return ResponseEntity.ok(responseMessage);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching ZooKeeper values: " + e.getMessage());
        }
    }

    @GetMapping("check-events")
    public ResponseEntity<?> checkGetQuery(@RequestParam("id") String webhookId){
//        return ResponseEntity.ok(webhookId);
//        return ResponseEntity.ok(Duration.parse("00:00:00.277"));
        return ResponseEntity.ok(webhookEventsRepository.findAllByWebhookId(webhookId));
    }
}
