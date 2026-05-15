package com.example.weathermap.controller;

import com.example.weathermap.config.ImdSchedulerProperties;
import com.example.weathermap.dto.CityWeatherPanelItemDto;
import com.example.weathermap.dto.DistrictWarningMapPointResponse;
import com.example.weathermap.dto.ImdSyncStatusResponse;
import com.example.weathermap.dto.NowcastMapPointResponse;
import com.example.weathermap.dto.RainfallMapPointResponse;
import com.example.weathermap.service.CityWeatherPanelService;
import com.example.weathermap.service.DistrictWarningMapService;
import com.example.weathermap.service.NowcastMapService;
import com.example.weathermap.service.RainfallMapService;
import com.example.weathermap.service.imd.ImdWeatherDataCache;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherApiController {

    private final NowcastMapService nowcastMapService;
    private final RainfallMapService rainfallMapService;
    private final DistrictWarningMapService districtWarningMapService;
    private final CityWeatherPanelService cityWeatherPanelService;
    private final ImdWeatherDataCache imdWeatherDataCache;
    private final ImdSchedulerProperties imdSchedulerProperties;

    public WeatherApiController(
            NowcastMapService nowcastMapService,
            RainfallMapService rainfallMapService,
            DistrictWarningMapService districtWarningMapService,
            CityWeatherPanelService cityWeatherPanelService,
            ImdWeatherDataCache imdWeatherDataCache,
            ImdSchedulerProperties imdSchedulerProperties
    ) {
        this.nowcastMapService = nowcastMapService;
        this.rainfallMapService = rainfallMapService;
        this.districtWarningMapService = districtWarningMapService;
        this.cityWeatherPanelService = cityWeatherPanelService;
        this.imdWeatherDataCache = imdWeatherDataCache;
        this.imdSchedulerProperties = imdSchedulerProperties;
    }

    @GetMapping("/sync-status")
    public ImdSyncStatusResponse getSyncStatus() {
        return new ImdSyncStatusResponse(
                imdWeatherDataCache.getNowcastRefreshedAt(),
                imdWeatherDataCache.getDailyRefreshedAt(),
                imdWeatherDataCache.getNowcastMap().size(),
                imdWeatherDataCache.getRainfallMap().size(),
                imdWeatherDataCache.getDistrictWarningMap().size(),
                imdWeatherDataCache.getCityWeatherPanels().size(),
                imdSchedulerProperties.nowcastCron(),
                imdSchedulerProperties.dailyCron()
        );
    }

    @GetMapping("/nowcast/map")
    public List<NowcastMapPointResponse> getNowcastMapData() {
        return nowcastMapService.getNowcastPointsForMap();
    }

    @GetMapping("/rainfall/map")
    public List<RainfallMapPointResponse> getRainfallMapData() {
        return rainfallMapService.getRainfallPointsForMap();
    }

    @GetMapping("/district-warning/map")
    public List<DistrictWarningMapPointResponse> getDistrictWarningMapData() {
        return districtWarningMapService.getWarningPointsForMap();
    }

    @GetMapping("/cities")
    public List<CityWeatherPanelItemDto> getCityWeatherPanels() {
        return cityWeatherPanelService.loadConfiguredStations();
    }
}
