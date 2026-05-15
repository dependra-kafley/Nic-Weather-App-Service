package com.example.weathermap.service;

import com.example.weathermap.dto.CityWeatherPanelDayDto;
import com.example.weathermap.dto.CityWeatherPanelItemDto;
import com.example.weathermap.dto.ImdCityWeatherResponse;
import com.example.weathermap.service.imd.ImdWeatherDataCache;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class CityWeatherPanelService {

    private final ImdCityWeatherApiClient imdCityWeatherApiClient;
    private final ImdWeatherDataCache cache;

    public CityWeatherPanelService(ImdCityWeatherApiClient imdCityWeatherApiClient, ImdWeatherDataCache cache) {
        this.imdCityWeatherApiClient = imdCityWeatherApiClient;
        this.cache = cache;
    }

    public List<CityWeatherPanelItemDto> loadConfiguredStations() {
        List<CityWeatherPanelItemDto> cached = cache.getCityWeatherPanels();
        if (!cached.isEmpty()) {
            return cached;
        }
        return fetchCityPanelsFromImd();
    }

    public List<CityWeatherPanelItemDto> fetchCityPanelsFromImd() {
        return imdCityWeatherApiClient.fetchAllConfiguredStations().stream()
                .map(CityWeatherPanelService::toPanelItem)
                .toList();
    }

    public static String iconTokenForForecast(String forecast) {
        if (forecast == null || forecast.isBlank()) {
            return "mixed";
        }
        String f = forecast.toLowerCase(Locale.ROOT);
        if (f.contains("thunder")) {
            return "thunder";
        }
        if (f.contains("rain") || f.contains("drizzle") || f.contains("shower")) {
            return "rain";
        }
        if (f.contains("fog") || f.contains("mist")) {
            return "fog";
        }
        if (f.contains("clear") || f.contains("sunny") || f.contains("mainly clear")) {
            return "clear";
        }
        if (f.contains("cloud")) {
            return "cloudy";
        }
        return "mixed";
    }

    public static CityWeatherPanelItemDto toPanelItem(ImdCityWeatherResponse r) {
        List<CityWeatherPanelDayDto> days = new ArrayList<>(7);
        String d1Max = firstNonBlank(r.todayMaxTemp(), r.todaysForecastMaxTemp());
        String d1Min = firstNonBlank(r.todayMinTemp(), r.todaysForecastMinTemp());
        String d1Fc = blankToDash(r.todaysForecast());
        days.add(new CityWeatherPanelDayDto(
                1,
                "Today",
                blankToDash(d1Max),
                blankToDash(d1Min),
                d1Fc,
                iconTokenForForecast(r.todaysForecast())
        ));
        days.add(dayRow(2, "Day 2", r.day2MaxTemp(), r.day2MinTemp(), r.day2Forecast()));
        days.add(dayRow(3, "Day 3", r.day3MaxTemp(), r.day3MinTemp(), r.day3Forecast()));
        days.add(dayRow(4, "Day 4", r.day4MaxTemp(), r.day4MinTemp(), r.day4Forecast()));
        days.add(dayRow(5, "Day 5", r.day5MaxTemp(), r.day5MinTemp(), r.day5Forecast()));
        days.add(dayRow(6, "Day 6", r.day6MaxTemp(), r.day6MinTemp(), r.day6Forecast()));
        days.add(dayRow(7, "Day 7", r.day7MaxTemp(), r.day7MinTemp(), r.day7Forecast()));

        String code = r.stationCode() != null ? r.stationCode() : "";
        String name = r.stationName() != null && !r.stationName().isBlank() ? r.stationName() : code;
        return new CityWeatherPanelItemDto(
                code,
                name,
                blankToDash(r.date()),
                blankToDash(r.past24HrsRainfall()),
                blankToDash(r.relativeHumidityAt0830()),
                blankToDash(r.sunriseTime()),
                blankToDash(r.sunsetTime()),
                days
        );
    }

    private static CityWeatherPanelDayDto dayRow(int n, String label, String max, String min, String forecast) {
        String fc = blankToDash(forecast);
        return new CityWeatherPanelDayDto(n, label, blankToDash(max), blankToDash(min), fc, iconTokenForForecast(forecast));
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    private static String blankToDash(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }
}
