package com.example.weathermap.service.imd;

import com.example.weathermap.config.ImdSchedulerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "weather.imd.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ImdWeatherDataScheduler {

    private static final Logger log = LoggerFactory.getLogger(ImdWeatherDataScheduler.class);

    private final ImdWeatherDataRefreshService refreshService;

    public ImdWeatherDataScheduler(ImdWeatherDataRefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @Scheduled(cron = "${weather.imd.scheduler.nowcast-cron:0 0 0/3 * * *}")
    public void scheduledNowcastRefresh() {
        log.debug("Nowcast scheduler triggered");
        refreshService.refreshNowcastFromImd();
    }

    @Scheduled(cron = "${weather.imd.scheduler.daily-cron:0 0 1 * * *}")
    public void scheduledDailyRefresh() {
        log.debug("Daily IMD scheduler triggered");
        refreshService.refreshDailyFromImd();
    }
}
