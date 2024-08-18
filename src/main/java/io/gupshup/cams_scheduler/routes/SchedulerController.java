package io.gupshup.cams_scheduler.routes;

import io.gupshup.cams_scheduler.services.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    @Autowired
    private SchedulerService schedulerService;

    @PostMapping("/update")
    public void updateSchedulerConfiguration() {
        schedulerService.updateConfiguration();
    }
}
