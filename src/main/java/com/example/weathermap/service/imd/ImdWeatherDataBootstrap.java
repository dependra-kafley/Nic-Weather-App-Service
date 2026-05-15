package com.example.weathermap.service.imd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ImdWeatherDataBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ImdWeatherDataBootstrap.class);

    private final ImdWeatherDataRefreshService refreshService;
    private final WeatherSnapshotStore snapshotStore;
    private final ImdWeatherDataCache cache;

    public ImdWeatherDataBootstrap(
            ImdWeatherDataRefreshService refreshService,
            WeatherSnapshotStore snapshotStore,
            ImdWeatherDataCache cache
    ) {
        this.refreshService = refreshService;
        this.snapshotStore = snapshotStore;
        this.cache = cache;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmCacheOnStartup() {
        log.info("Loading latest weather snapshots from H2");
        snapshotStore.loadAllIntoCache(cache);

        boolean hasCachedData = !cache.getNowcastMap().isEmpty()
                || !cache.getRainfallMap().isEmpty()
                || !cache.getCityWeatherPanels().isEmpty();

        if (hasCachedData) {
            log.info(
                    "H2 warm-up complete: nowcast={}, rainfall={}, warnings={}, cities={}",
                    cache.getNowcastMap().size(),
                    cache.getRainfallMap().size(),
                    cache.getDistrictWarningMap().size(),
                    cache.getCityWeatherPanels().size()
            );
            return;
        }

        log.info("No H2 snapshots found — fetching from IMD APIs");
        refreshService.refreshAllFromImd();
    }
}
