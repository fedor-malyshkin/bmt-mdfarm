package test.bmt.mdfarm.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import test.bmt.mdfarm.service.MobileDevicesFarmService;

import javax.annotation.PostConstruct;

@EnableScheduling
@Configuration
@Slf4j
@RequiredArgsConstructor
public class ScheduleConfig {
    private final MobileDevicesFarmService service;

    @Value("${app.disable-sync}")
    private boolean disableSync;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncSpecifications() {
        // for not a scheduled env - run  on startup (important for all envs)
        try {
            if (!disableSync)
                service.updateSpecification();
        } catch (Exception e) {
            log.error("SCHEDULE: syncSpecifications", e);
        }
    }

}
