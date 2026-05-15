package com.example.weathermap.dto;

public record CityWeatherPanelDayDto(
        int dayNumber,
        String label,
        String maxTemp,
        String minTemp,
        String forecastText,
        String iconToken
) {
}
