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

    public ImdWeatherDataRefreshService(
            NowcastMapService nowcastMapService,
            RainfallMapService rainfallMapService,
            DistrictWarningMapService districtWarningMapService,
            CityWeatherPanelService cityWeatherPanelService,
            AwsDataService awsDataService,
            ImdWeatherDataCache cache,
            WeatherSnapshotStore snapshotStore
    ) {
        this.nowcastMapService = nowcastMapService;
        this.rainfallMapService = rainfallMapService;
        this.districtWarningMapService = districtWarningMapService;
        this.cityWeatherPanelService = cityWeatherPanelService;
        this.awsDataService = awsDataService;
        this.cache = cache;
        this.snapshotStore = snapshotStore;
    }

    public void refreshNowcastFromImd() {
        log.info("Refreshing IMD district nowcast data");
        List<NowcastMapPointResponse> data = nowcastMapService.fetchNowcastPointsFromImd();
        cache.updateNowcast(data);
        snapshotStore.saveNowcast(data);
        log.info("Nowcast cache and H2 snapshot updated: {} districts", data.size());
    }

    public void refreshDailyFromImd() {
        log.info("Refreshing IMD daily data (rainfall, district warning, city weather, AWS)");
        List<RainfallMapPointResponse> rainfall = rainfallMapService.fetchRainfallPointsFromImd();
        List<DistrictWarningMapPointResponse> warnings = districtWarningMapService.fetchWarningPointsFromImd();
        List<CityWeatherPanelItemDto> cities = cityWeatherPanelService.fetchCityPanelsFromImd();
        ImdAwsDataGroupedResponse aws = awsDataService.fetchGroupedAwsDataFromImd();

        cache.updateDaily(rainfall, warnings, cities, aws);
        snapshotStore.saveRainfall(rainfall);
        snapshotStore.saveWarning(warnings);
        snapshotStore.saveCityWeather(cities);
        snapshotStore.saveAwsData(aws);

        log.info(
                "Daily cache and H2 snapshots updated: rainfall={}, warnings={}, cities={}, aws stations={}",
                rainfall.size(),
                warnings.size(),
                cities.size(),
                aws.aws().size() + aws.org().size() + aws.arg().size()
        );
    }

    public void refreshAllFromImd() {
        refreshNowcastFromImd();
        refreshDailyFromImd();
    }
}
