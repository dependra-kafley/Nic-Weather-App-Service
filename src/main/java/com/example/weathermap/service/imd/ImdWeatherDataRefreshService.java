package com.example.weathermap.service.imd;

import com.example.weathermap.dto.CityWeatherPanelItemDto;
import com.example.weathermap.dto.DistrictWarningMapPointResponse;
import com.example.weathermap.dto.ImdAwsDataGroupedResponse;
import com.example.weathermap.dto.NowcastMapPointResponse;
import com.example.weathermap.dto.RainfallMapPointResponse;
import com.example.weathermap.service.AwsDataService;
import com.example.weathermap.service.CityWeatherPanelService;
import com.example.weathermap.service.DistrictWarningMapService;
import com.example.weathermap.service.NowcastMapService;
import com.example.weathermap.service.RainfallMapService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ImdWeatherDataRefreshService {

    private static final Logger log = LoggerFactory.getLogger(ImdWeatherDataRefreshService.class);

    private final NowcastMapService nowcastMapService;
    private final RainfallMapService rainfallMapService;
    private final DistrictWarningMapService districtWarningMapService;
    private final CityWeatherPanelService cityWeatherPanelService;
    private final AwsDataService awsDataService;
    private final ImdWeatherDataCache cache;
    private final WeatherSnapshotStore snapshotStore;
    private final ImdApiCallAudit apiCallAudit;

    public ImdWeatherDataRefreshService(
            NowcastMapService nowcastMapService,
            RainfallMapService rainfallMapService,
            DistrictWarningMapService districtWarningMapService,
            CityWeatherPanelService cityWeatherPanelService,
            AwsDataService awsDataService,
            ImdWeatherDataCache cache,
            WeatherSnapshotStore snapshotStore,
            ImdApiCallAudit apiCallAudit
    ) {
        this.nowcastMapService = nowcastMapService;
        this.rainfallMapService = rainfallMapService;
        this.districtWarningMapService = districtWarningMapService;
        this.cityWeatherPanelService = cityWeatherPanelService;
        this.awsDataService = awsDataService;
        this.cache = cache;
        this.snapshotStore = snapshotStore;
        this.apiCallAudit = apiCallAudit;
    }

    public void refreshNowcastFromImd() {
        apiCallAudit.refreshStarted("nowcast");
        runIsolated("nowcast", this::refreshNowcastFromImdInternal);
    }

    public void refreshDailyFromImd() {
        apiCallAudit.refreshStarted("daily");
        runIsolated("rainfall", this::refreshRainfallFromImd);
        runIsolated("district-warning", this::refreshDistrictWarningsFromImd);
        runIsolated("city-weather", this::refreshCityWeatherFromImd);
        runIsolated("aws-data", this::refreshAwsDataFromImd);
        apiCallAudit.refreshFinished("daily", "COMPLETE", "see per-api IMD_REFRESH_END lines");
    }

    public void refreshAllFromImd() {
        apiCallAudit.refreshStarted("full");
        runIsolated("nowcast", this::refreshNowcastFromImdInternal);
        runIsolated("rainfall", this::refreshRainfallFromImd);
        runIsolated("district-warning", this::refreshDistrictWarningsFromImd);
        runIsolated("city-weather", this::refreshCityWeatherFromImd);
        runIsolated("aws-data", this::refreshAwsDataFromImd);
        apiCallAudit.refreshFinished("full", "COMPLETE", "all API groups attempted");
    }

    private void refreshNowcastFromImdInternal() {
        log.info("Refreshing IMD district nowcast data");
        List<NowcastMapPointResponse> data = nowcastMapService.fetchNowcastPointsFromImd();
        cache.updateNowcast(data);
        snapshotStore.saveNowcast(data);
        log.info("Nowcast cache and H2 snapshot updated: {} districts", data.size());
        apiCallAudit.refreshFinished("nowcast", "OK", data.size() + " districts");
    }

    private void refreshRainfallFromImd() {
        log.info("Refreshing IMD district rainfall data");
        List<RainfallMapPointResponse> data = rainfallMapService.fetchRainfallPointsFromImd();
        cache.updateRainfall(data);
        snapshotStore.saveRainfall(data);
        log.info("Rainfall cache and H2 snapshot updated: {} districts", data.size());
        apiCallAudit.refreshFinished("rainfall", data.isEmpty() ? "EMPTY" : "OK", data.size() + " districts");
    }

    private void refreshDistrictWarningsFromImd() {
        log.info("Refreshing IMD district warning data");
        List<DistrictWarningMapPointResponse> data = districtWarningMapService.fetchWarningPointsFromImd();
        cache.updateDistrictWarnings(data);
        snapshotStore.saveWarning(data);
        log.info("Warning cache and H2 snapshot updated: {} districts", data.size());
        apiCallAudit.refreshFinished("district-warning", data.isEmpty() ? "EMPTY" : "OK", data.size() + " districts");
    }

    private void refreshCityWeatherFromImd() {
        log.info("Refreshing IMD city weather data");
        List<CityWeatherPanelItemDto> data = cityWeatherPanelService.fetchCityPanelsFromImd();
        cache.updateCityWeather(data);
        snapshotStore.saveCityWeather(data);
        log.info("City weather cache and H2 snapshot updated: {} stations", data.size());
        apiCallAudit.refreshFinished("city-weather", data.isEmpty() ? "EMPTY" : "OK", data.size() + " stations");
    }

    private void refreshAwsDataFromImd() {
        log.info("Refreshing IMD AWS/ARG station data");
        ImdAwsDataGroupedResponse data = awsDataService.fetchGroupedAwsDataFromImd();
        cache.updateAwsData(data);
        snapshotStore.saveAwsData(data);
        int count = data.org().size() + data.aws().size() + data.arg().size();
        log.info("AWS cache and H2 snapshot updated: {} stations", count);
        apiCallAudit.refreshFinished("aws-data", count == 0 ? "EMPTY" : "OK", count + " stations");
    }

    private void runIsolated(String apiGroup, Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            log.error("[{}] Refresh failed (other APIs continue): {}", apiGroup, ex.getMessage(), ex);
            apiCallAudit.refreshFinished(apiGroup, "FAIL", ex.getMessage());
        }
    }

}
