package com.example.weathermap.service.imd;

import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ImdWeatherDataBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ImdWeatherDataBootstrap.class);

    private final ImdWeatherDataRefreshService refreshService;
    private final WeatherSnapshotStore snapshotStore;
    private final ImdWeatherDataCache cache;
    private final Executor weatherRefreshExecutor;

    public ImdWeatherDataBootstrap(
            ImdWeatherDataRefreshService refreshService,
            WeatherSnapshotStore snapshotStore,
            ImdWeatherDataCache cache,
            @Qualifier("weatherRefreshExecutor") Executor weatherRefreshExecutor
    ) {
        this.refreshService = refreshService;
        this.snapshotStore = snapshotStore;
        this.cache = cache;
        this.weatherRefreshExecutor = weatherRefreshExecutor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmCacheOnStartup() {
        log.info("Loading weather snapshots from H2 into memory");
        snapshotStore.loadAllIntoCache(cache);
        var aws = cache.getAwsData();
        int awsCount = aws.org().size() + aws.aws().size() + aws.arg().size();
        log.info(
                "H2 cache ready: nowcast={}, rainfall={}, warnings={}, cities={}, aws={}",
                cache.getNowcastMap().size(),
                cache.getRainfallMap().size(),
                cache.getDistrictWarningMap().size(),
                cache.getCityWeatherPanels().size(),
                awsCount
        );

        weatherRefreshExecutor.execute(() -> {
            log.info(
                    "Non-blocking startup refresh started (app is already serving; see ./logs/nic-weather-app.log for IMD_CALL_* audit lines)"
            );
            try {
                refreshService.refreshAllFromImd();
                log.info("Non-blocking startup refresh completed — all API groups attempted");
            } catch (Exception ex) {
                log.error("Unexpected error during startup refresh: {}", ex.getMessage(), ex);
            }
        });
    }
}
