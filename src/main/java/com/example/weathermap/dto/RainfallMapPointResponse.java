package com.example.weathermap.dto;

public record RainfallMapPointResponse(
        String districtKey,
        String objId,
        String district,
        String date,
        String dailyActual,
        String dailyNormal,
        String dailyDeparturePer,
        String dailyCategory,
        String dailyCategoryLabel,
        String colorHex,
        String weekDate,
        String weeklyActual,
        String weeklyNormal,
        String weeklyDeparturePer,
        String weeklyCategory,
        String cumulativeDate,
        String cumulativeActual,
        String cumulativeNormal,
        String cumulativeDeparturePer,
        String cumulativeCategory,
        String monthlyDate,
        String monthlyActual,
        String monthlyNormal,
        String monthlyDeparturePer,
        String monthlyCategory
) {
}
