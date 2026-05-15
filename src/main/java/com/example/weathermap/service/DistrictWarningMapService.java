package com.example.weathermap.service;

import com.example.weathermap.dto.DistrictWarningMapPointResponse;
import com.example.weathermap.dto.DistrictWarningMapPointResponse.WarningDayDto;
import com.example.weathermap.dto.ImdDistrictWarningResponse;
import com.example.weathermap.service.imd.ImdWeatherDataCache;
import com.example.weathermap.util.DistrictNameNormalizer;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DistrictWarningMapService {

    private final ImdDistrictWarningApiClient apiClient;
    private final ImdWeatherDataCache cache;

    public DistrictWarningMapService(ImdDistrictWarningApiClient apiClient, ImdWeatherDataCache cache) {
        this.apiClient = apiClient;
        this.cache = cache;
    }

    public List<DistrictWarningMapPointResponse> getWarningPointsForMap() {
        List<DistrictWarningMapPointResponse> cached = cache.getDistrictWarningMap();
        if (!cached.isEmpty()) {
            return cached;
        }
        return fetchWarningPointsFromImd();
    }

    public List<DistrictWarningMapPointResponse> fetchWarningPointsFromImd() {
        return apiClient.fetchAllConfiguredDistricts().stream()
                .map(this::toMapPoint)
                .toList();
    }

    private DistrictWarningMapPointResponse toMapPoint(ImdDistrictWarningResponse r) {
        String mapColor = blankToDash(r.day1Color());
        List<WarningDayDto> days = buildDays(r);
        return new DistrictWarningMapPointResponse(
                DistrictNameNormalizer.toDistrictKey(r.district()),
                blankToDash(r.objId()),
                blankToDash(r.district()),
                blankToDash(r.date()),
                blankToDash(r.utc()),
                mapColor,
                warningColorToHex(mapColor),
                days
        );
    }

    private static List<WarningDayDto> buildDays(ImdDistrictWarningResponse r) {
        List<WarningDayDto> days = new ArrayList<>(5);
        days.add(dayRow(1, "Day 1", r.day1(), r.day1Color()));
        days.add(dayRow(2, "Day 2", r.day2(), r.day2Color()));
        days.add(dayRow(3, "Day 3", r.day3(), r.day3Color()));
        days.add(dayRow(4, "Day 4", r.day4(), r.day4Color()));
        days.add(dayRow(5, "Day 5", r.day5(), r.day5Color()));
        return days;
    }

    private static WarningDayDto dayRow(int n, String label, String codes, String color) {
        String normalizedCodes = blankToDash(codes);
        String colorCode = blankToDash(color);
        return new WarningDayDto(
                n,
                label,
                normalizedCodes,
                summarizeWarningCodes(normalizedCodes),
                colorCode,
                warningColorToHex(colorCode)
        );
    }

    static String summarizeWarningCodes(String codesCsv) {
        if (codesCsv == null || codesCsv.isBlank() || "—".equals(codesCsv)) {
            return "No warning";
        }
        String[] parts = codesCsv.split(",");
        StringBuilder b = new StringBuilder();
        for (String part : parts) {
            String code = part.trim();
            if (code.isEmpty()) {
                continue;
            }
            if (b.length() > 0) {
                b.append("; ");
            }
            b.append(warningCodeLabel(code));
        }
        return b.length() == 0 ? "No warning" : b.toString();
    }

    static String warningCodeLabel(String code) {
        return switch (code.trim()) {
            case "1" -> "No warning";
            case "2" -> "Heavy rain";
            case "3" -> "Heavy snow";
            case "4" -> "Thunderstorm & lightning, squall";
            case "5" -> "Hailstorm";
            case "6" -> "Dust storm";
            case "7" -> "Dust raising winds";
            case "8" -> "Strong surface winds";
            case "9" -> "Heat wave";
            case "10" -> "Hot day";
            case "11" -> "Warm night";
            case "12" -> "Cold wave";
            case "13" -> "Cold day";
            case "14" -> "Ground frost";
            case "15" -> "Fog";
            case "16" -> "Very heavy rain";
            case "17" -> "Extremely heavy rain";
            default -> "Code " + code;
        };
    }

    /**
     * IMD district warning day colours: 1 red, 2 orange, 3 yellow, 4 green.
     */
    static String warningColorToHex(String colorCode) {
        if (colorCode == null || colorCode.isBlank() || "—".equals(colorCode)) {
            return "#cbd5e1";
        }
        return switch (colorCode.trim()) {
            case "1" -> "#FF0000";
            case "2" -> "#ffa500";
            case "3" -> "#ffff00";
            case "4" -> "#7cfc00";
            default -> "#cbd5e1";
        };
    }

    private static String blankToDash(String s) {
        return s == null || s.isBlank() ? "—" : s.trim();
    }
}
