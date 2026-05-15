package com.example.weathermap.dto;

import java.util.List;

public record DistrictWarningMapPointResponse(
        String districtKey,
        String objId,
        String district,
        String date,
        String utc,
        String mapColor,
        String colorHex,
        List<WarningDayDto> days
) {

    public record WarningDayDto(
            int dayNumber,
            String label,
            String warningCodes,
            String warningSummary,
            String color,
            String colorHex
    ) {
    }
}
