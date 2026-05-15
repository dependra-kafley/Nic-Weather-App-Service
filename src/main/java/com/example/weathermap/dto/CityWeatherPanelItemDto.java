package com.example.weathermap.dto;

import java.util.List;

public record CityWeatherPanelItemDto(
        String stationCode,
        String stationName,
        String observationDate,
        String past24HrsRainfall,
        String relativeHumidityAt0830,
        String sunriseTime,
        String sunsetTime,
        List<CityWeatherPanelDayDto> days
) {
}
