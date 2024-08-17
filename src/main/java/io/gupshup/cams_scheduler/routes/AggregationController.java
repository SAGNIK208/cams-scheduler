package io.gupshup.cams_scheduler.routes;

import io.gupshup.cams_scheduler.services.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aggregation")
public class AggregationController {

    @Autowired
    private AggregationService aggregationService;

    @PostMapping("/sync")
    public String triggerFullAggregation() {
        try {
            aggregationService.triggerFullAggregation();
            return "Aggregation triggered successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to trigger aggregation";
        }
    }
}
