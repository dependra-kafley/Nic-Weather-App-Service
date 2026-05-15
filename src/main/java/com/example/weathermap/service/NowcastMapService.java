package com.example.weathermap.service;

import com.example.weathermap.dto.ImdNowcastDistrictResponse;
import com.example.weathermap.dto.NowcastMapPointResponse;
import com.example.weathermap.service.imd.ImdWeatherDataCache;
import com.example.weathermap.util.DistrictNameNormalizer;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NowcastMapService {

    private final ImdNowcastDistrictApiClient imdNowcastDistrictApiClient;
    private final ImdWeatherDataCache cache;

    public NowcastMapService(ImdNowcastDistrictApiClient imdNowcastDistrictApiClient, ImdWeatherDataCache cache) {
        this.imdNowcastDistrictApiClient = imdNowcastDistrictApiClient;
        this.cache = cache;
    }

    public List<NowcastMapPointResponse> getNowcastPointsForMap() {
        List<NowcastMapPointResponse> cached = cache.getNowcastMap();
        if (!cached.isEmpty()) {
            return cached;
        }
        return fetchNowcastPointsFromImd();
    }

    public List<NowcastMapPointResponse> fetchNowcastPointsFromImd() {
        return imdNowcastDistrictApiClient.fetchAllConfiguredDistricts().stream()
                .map(this::toMapPoint)
                .toList();
    }

    private NowcastMapPointResponse toMapPoint(ImdNowcastDistrictResponse r) {
        String key = DistrictNameNormalizer.toDistrictKey(r.stateDistrict());
        String hex = colorCodeToHex(r.color());
        String summary = buildShortSummary(r);
        return new NowcastMapPointResponse(
                key,
                r.objId(),
                r.stateDistrict(),
                r.date() != null ? r.date().toString() : "",
                r.color() != null ? r.color() : "",
                hex,
                r.toi() != null ? r.toi() : "",
                r.vupto() != null ? r.vupto() : "",
                r.message() != null ? r.message() : "",
                summary
        );
    }

    static String colorCodeToHex(String colorCode) {
        if (colorCode == null) {
            return "#cfe2ff";
        }
        return switch (colorCode.trim()) {
            case "1" -> "#008000";
            case "2" -> "#FFFF00";
            case "3" -> "#FFA500";
            case "4" -> "#ff0000";
            default -> "#cfe2ff";
        };
    }

    private static String buildShortSummary(ImdNowcastDistrictResponse r) {
        StringBuilder b = new StringBuilder();
        appendCat(b, r.cat2(), "Light rain");
        appendCat(b, r.cat3(), "Light snow");
        appendCat(b, r.cat4(), "Light thunderstorms");
        appendCat(b, r.cat7(), "Moderate rain");
        appendCat(b, r.cat12(), "Heavy rain");
        appendCat(b, r.cat14(), "Severe thunderstorms");
        appendCat(b, r.cat15(), "Very severe thunderstorms");
        if (r.message() != null && !r.message().isBlank()) {
            if (b.length() > 0) {
                b.append(" · ");
            }
            b.append(r.message().trim());
        }
        if (b.length() == 0) {
            return "No significant warning (Cat1 / dry)";
        }
        return b.toString();
    }

    private static void appendCat(StringBuilder b, String value, String label) {
        if (value == null || "0".equals(value.trim()) || value.isBlank()) {
            return;
        }
        if (b.length() > 0) {
            b.append("; ");
        }
        b.append(label);
    }
}
