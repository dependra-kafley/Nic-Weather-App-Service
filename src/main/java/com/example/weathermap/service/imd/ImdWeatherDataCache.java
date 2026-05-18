package com.example.weathermap.service.imd;

import com.example.weathermap.dto.CityWeatherPanelItemDto;
import com.example.weathermap.dto.DistrictWarningMapPointResponse;
import com.example.weathermap.dto.ImdAwsDataGroupedResponse;
import com.example.weathermap.dto.NowcastMapPointResponse;
import com.example.weathermap.dto.RainfallMapPointResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ImdWeatherDataCache {

    private volatile List<NowcastMapPointResponse> nowcastMap = List.of();
    private volatile Instant nowcastRefreshedAt;

    private volatile List<RainfallMapPointResponse> rainfallMap = List.of();
    private volatile List<DistrictWarningMapPointResponse> districtWarningMap = List.of();
    private volatile List<CityWeatherPanelItemDto> cityWeatherPanels = List.of();
    private volatile ImdAwsDataGroupedResponse awsData =
            new ImdAwsDataGroupedResponse(List.of(), List.of(), List.of());
    private volatile Instant dailyRefreshedAt;

    public List<NowcastMapPointResponse> getNowcastMap() {
        return nowcastMap;
    }

    public Instant getNowcastRefreshedAt() {
        return nowcastRefreshedAt;
    }

    public List<RainfallMapPointResponse> getRainfallMap() {
        return rainfallMap;
    }

    public List<DistrictWarningMapPointResponse> getDistrictWarningMap() {
        return districtWarningMap;
    }

    public List<CityWeatherPanelItemDto> getCityWeatherPanels() {
        return cityWeatherPanels;
    }

    public ImdAwsDataGroupedResponse getAwsData() {
        return awsData;
    }

    public Instant getDailyRefreshedAt() {
        return dailyRefreshedAt;
    }

    /**
     * Best-effort date from the latest daily payloads (rainfall preferred).
     */
    public String getLastDailyDateStr() {
        return rainfallMap.stream()
                .map(RainfallMapPointResponse::date)
                .filter(ImdWeatherDataCache::isPresentDate)
                .findFirst()
                .or(() -> districtWarningMap.stream()
                        .map(DistrictWarningMapPointResponse::date)
                        .filter(ImdWeatherDataCache::isPresentDate)
                        .findFirst())
                .or(() -> cityWeatherPanels.stream()
                        .map(CityWeatherPanelItemDto::observationDate)
                        .filter(ImdWeatherDataCache::isPresentDate)
                        .findFirst())
                .orElse(null);
    }

    private static boolean isPresentDate(String value) {
        return value != null && !value.isBlank() && !"—".equals(value);
    }

    public void updateNowcast(List<NowcastMapPointResponse> data) {
        this.nowcastMap = List.copyOf(data);
        this.nowcastRefreshedAt = Instant.now();
    }

    public void updateDaily(
            List<RainfallMapPointResponse> rainfall,
            List<DistrictWarningMapPointResponse> warnings,
            List<CityWeatherPanelItemDto> cities
    ) {
        updateDaily(rainfall, warnings, cities,
                new ImdAwsDataGroupedResponse(List.of(), List.of(), List.of()));
    }

    public void updateDaily(
            List<RainfallMapPointResponse> rainfall,
            List<DistrictWarningMapPointResponse> warnings,
            List<CityWeatherPanelItemDto> cities,
            ImdAwsDataGroupedResponse aws
    ) {
        this.rainfallMap = List.copyOf(rainfall);
        this.districtWarningMap = List.copyOf(warnings);
        this.cityWeatherPanels = List.copyOf(cities);
        this.awsData = aws != null
                ? aws
                : new ImdAwsDataGroupedResponse(List.of(), List.of(), List.of());
        this.dailyRefreshedAt = Instant.now();
    }

    public void updateRainfall(List<RainfallMapPointResponse> data) {
        this.rainfallMap = List.copyOf(data);
        this.dailyRefreshedAt = Instant.now();
    }

    public void updateDistrictWarnings(List<DistrictWarningMapPointResponse> data) {
        this.districtWarningMap = List.copyOf(data);
        this.dailyRefreshedAt = Instant.now();
    }

    public void updateCityWeather(List<CityWeatherPanelItemDto> data) {
        this.cityWeatherPanels = List.copyOf(data);
        this.dailyRefreshedAt = Instant.now();
    }

    public void updateAwsData(ImdAwsDataGroupedResponse data) {
        this.awsData = data != null
                ? data
                : new ImdAwsDataGroupedResponse(List.of(), List.of(), List.of());
        this.dailyRefreshedAt = Instant.now();
    }
}
