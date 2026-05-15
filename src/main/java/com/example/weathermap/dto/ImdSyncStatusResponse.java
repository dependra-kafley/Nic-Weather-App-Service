package com.example.weathermap.dto;

import java.time.Instant;

public record ImdSyncStatusResponse(
        Instant nowcastLastRefreshedAt,
        Instant dailyLastRefreshedAt,
        int nowcastDistrictCount,
        int rainfallDistrictCount,
        int districtWarningCount,
        int cityStationCount,
        String nowcastSchedule,
        String dailySchedule
) {
}
