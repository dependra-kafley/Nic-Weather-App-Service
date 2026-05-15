package com.example.weathermap.service;

import com.example.weathermap.dto.ImdDistrictRainfallResponse;
import com.example.weathermap.dto.RainfallMapPointResponse;
import com.example.weathermap.service.imd.ImdWeatherDataCache;
import com.example.weathermap.util.DistrictNameNormalizer;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RainfallMapService {

    private final ImdDistrictRainfallApiClient imdDistrictRainfallApiClient;
    private final ImdWeatherDataCache cache;

    public RainfallMapService(ImdDistrictRainfallApiClient imdDistrictRainfallApiClient, ImdWeatherDataCache cache) {
        this.imdDistrictRainfallApiClient = imdDistrictRainfallApiClient;
        this.cache = cache;
    }

    public List<RainfallMapPointResponse> getRainfallPointsForMap() {
        List<RainfallMapPointResponse> cached = cache.getRainfallMap();
        if (!cached.isEmpty()) {
            return cached;
        }
        return fetchRainfallPointsFromImd();
    }

    public List<RainfallMapPointResponse> fetchRainfallPointsFromImd() {
        return imdDistrictRainfallApiClient.fetchAllConfiguredDistricts().stream()
                .map(this::toMapPoint)
                .toList();
    }

    private RainfallMapPointResponse toMapPoint(ImdDistrictRainfallResponse r) {
        String category = blankToDash(r.dailyCategory());
        return new RainfallMapPointResponse(
                DistrictNameNormalizer.toDistrictKey(r.district()),
                blankToDash(r.objId()),
                blankToDash(r.district()),
                blankToDash(r.date()),
                blankToDash(r.dailyActual()),
                blankToDash(r.dailyNormal()),
                blankToDash(r.dailyDeparturePer()),
                category,
                categoryLabel(category),
                categoryToHex(category),
                blankToDash(r.weekDate()),
                blankToDash(r.weeklyActual()),
                blankToDash(r.weeklyNormal()),
                blankToDash(r.weeklyDeparturePer()),
                blankToDash(r.weeklyCategory()),
                blankToDash(r.cumulativeDate()),
                blankToDash(r.cumulativeActual()),
                blankToDash(r.cumulativeNormal()),
                blankToDash(r.cumulativeDeparturePer()),
                blankToDash(r.cumulativeCategory()),
                cleanMonthlyDate(r.monthlyDate()),
                blankToDash(r.monthlyActual()),
                blankToDash(r.monthlyNormal()),
                blankToDash(r.monthlyDeparturePer()),
                blankToDash(r.monthlyCategory())
        );
    }

    static String categoryLabel(String code) {
        if (code == null || code.isBlank() || "—".equals(code)) {
            return "No data";
        }
        return switch (code.trim().toUpperCase()) {
            case "LE" -> "Large excess (≥60%)";
            case "E" -> "Excess (20% to 59%)";
            case "N" -> "Normal (-19% to 19%)";
            case "D" -> "Deficient (-59% to -20%)";
            case "LD" -> "Large deficient (-99% to -60%)";
            case "NR" -> "No rain (-100%)";
            case "ND" -> "No data";
            default -> code;
        };
    }

    static String categoryToHex(String code) {
        if (code == null || code.isBlank()) {
            return "#cbd5e1";
        }
        return switch (code.trim().toUpperCase()) {
            case "LE" -> "#1d4ed8";
            case "E" -> "#22c55e";
            case "N" -> "#fef08a";
            case "D" -> "#fb923c";
            case "LD" -> "#ef4444";
            case "NR" -> "#991b1b";
            case "ND" -> "#94a3b8";
            default -> "#cbd5e1";
        };
    }

    private static String cleanMonthlyDate(String value) {
        if (value == null) {
            return "—";
        }
        return value.replace("\r", "").trim();
    }

    private static String blankToDash(String s) {
        return s == null || s.isBlank() ? "—" : s.trim();
    }
}
