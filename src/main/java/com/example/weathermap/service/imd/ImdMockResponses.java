package com.example.weathermap.service.imd;

import com.example.weathermap.dto.ImdAwsDataResponse;
import com.example.weathermap.dto.ImdCityWeatherResponse;
import com.example.weathermap.dto.ImdDistrictRainfallResponse;
import com.example.weathermap.dto.ImdDistrictWarningResponse;
import com.example.weathermap.dto.ImdNowcastDistrictResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ImdMockResponses {

    private ImdMockResponses() {
    }

    public static ImdNowcastDistrictResponse nowcast(String objId, ObjectMapper mapper) {
        return mapper.convertValue(buildNowcastMap(objId), ImdNowcastDistrictResponse.class);
    }

    private static Map<String, Object> buildNowcastMap(String objId) {
        LocalDate today = LocalDate.now();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Obj_id", objId);
        map.put("State_District", meghalayaDistrictName(objId));
        map.put("Date", today.toString());
        putCats(map, "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "", "0", "0", "0");
        switch (objId) {
            case "1" -> {
                putCats(map, "0", "2", "0", "4", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "", "0", "0", "0");
                map.put("message", "Light rain and thunderstorms likely");
                map.put("color", "2");
            }
            case "2" -> {
                putCats(map, "0", "0", "0", "0", "0", "0", "7", "0", "0", "0", "0", "0", "0", "0", "0", "", "0", "0", "0");
                map.put("message", "Moderate rain expected");
                map.put("color", "3");
            }
            case "3" -> {
                putCats(map, "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "12", "0", "0", "14", "0", "", "0", "0", "0");
                map.put("message", "Heavy rain warning");
                map.put("color", "4");
            }
            case "4" -> {
                putCats(map, "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "", "0", "0", "0");
                map.put("color", "1");
            }
            case "5" -> {
                putCats(map, "0", "2", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "", "0", "0", "0");
                map.put("color", "2");
            }
            case "6" -> {
                putCats(map, "0", "0", "0", "4", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "", "0", "0", "0");
                map.put("color", "3");
            }
            case "7" -> {
                putCats(map, "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "15", "0", "Fog likely", "0", "0", "0");
                map.put("color", "2");
            }
            case "575" -> {
                putCats(map, "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "7", "0", "0", "0", "0", "", "0", "0", "0");
                map.put("color", "3");
            }
            case "672" -> {
                putCats(map, "0", "2", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "", "0", "0", "0");
                map.put("color", "2");
            }
            case "673" -> {
                putCats(map, "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "12", "0", "0", "", "0", "0", "0");
                map.put("color", "4");
            }
            case "674" -> {
                putCats(map, "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "", "0", "0", "0");
                map.put("color", "1");
            }
            default -> map.put("color", nowcastColor(objId));
        }
        if (!map.containsKey("message")) {
            map.put("message", "");
        }
        if (!map.containsKey("color")) {
            map.put("color", "2");
        }
        map.put("toi", "1430");
        map.put("vupto", "1730");
        return map;
    }

    private static void putCats(
            Map<String, Object> map,
            String c1, String c2, String c3, String c4, String c5, String c6, String c7,
            String c8, String c9, String c10, String c11, String c12, String c13, String c14, String c15,
            String c16, String c17, String c18, String c19
    ) {
        map.put("cat1", c1);
        map.put("cat2", c2);
        map.put("cat3", c3);
        map.put("cat4", c4);
        map.put("cat5", c5);
        map.put("cat6", c6);
        map.put("cat7", c7);
        map.put("cat8", c8);
        map.put("cat9", c9);
        map.put("cat10", c10);
        map.put("cat11", c11);
        map.put("cat12", c12);
        map.put("cat13", c13);
        map.put("cat14", c14);
        map.put("cat15", c15);
        map.put("cat16", c16);
        map.put("cat17", c17);
        map.put("cat18", c18);
        map.put("cat19", c19);
    }

    public static ImdDistrictRainfallResponse rainfall(String objId) {
        return new ImdDistrictRainfallResponse(
                objId,
                meghalayaDistrictName(objId),
                "2026-05-13",
                rainfallDailyActual(objId),
                "1.70",
                rainfallDailyDeparture(objId),
                rainfallDailyCategory(objId),
                "06-05-2026 To 12-05-2026",
                "12.40",
                "8.50",
                "46%",
                "E",
                "2026-01-01",
                "245.60",
                "220.00",
                "12%",
                "N",
                "01-04-2026 To 30-04-2026",
                "85.10",
                "80.00",
                "6%",
                "N"
        );
    }

    public static ImdDistrictWarningResponse districtWarning(String objId) {
        String[] codes = warningDayCodes(objId);
        String[] colors = warningDayColors(objId);
        return new ImdDistrictWarningResponse(
                objId,
                "2026-05-13",
                "06:30",
                meghalayaDistrictName(objId),
                codes[0],
                codes[1],
                codes[2],
                codes[3],
                codes[4],
                colors[0],
                colors[1],
                colors[2],
                colors[3],
                colors[4]
        );
    }

    public static ImdCityWeatherResponse city(String stationId, ObjectMapper mapper) {
        if ("99489".equals(stationId)) {
            return readJson(mapper, TURA_JSON, ImdCityWeatherResponse.class);
        }
        if ("42516".equals(stationId)) {
            return readJson(mapper, SHILLONG_JSON, ImdCityWeatherResponse.class);
        }
        return readJson(mapper, """
                {
                  "Date": "2026-05-13",
                  "Station_Code": "%s",
                  "Station_Name": "STATION %s",
                  "Today_Max_temp": null,
                  "Today_Min_temp": "20.0",
                  "Past_24_hrs_Rainfall": "0",
                  "Relative_Humidity_at_0830": "70",
                  "Sunset_time": "18:00",
                  "Sunrise_time": "05:00",
                  "Todays_Forecast_Max_Temp": "30.0",
                  "Todays_Forecast_Min_temp": "20.0",
                  "Todays_Forecast": "Partly cloudy sky",
                  "Day_2_Max_Temp": "30.0",
                  "Day_2_Min_temp": "20.0",
                  "Day_2_Forecast": "Partly cloudy sky",
                  "Day_3_Max_Temp": "30.0",
                  "Day_3_Min_temp": "20.0",
                  "Day_3_Forecast": "Partly cloudy sky",
                  "Day_4_Max_Temp": "30.0",
                  "Day_4_Min_temp": "20.0",
                  "Day_4_Forecast": "Partly cloudy sky",
                  "Day_5_Max_Temp": "30.0",
                  "Day_5_Min_temp": "20.0",
                  "Day_5_Forecast": "Partly cloudy sky",
                  "Day_6_Max_Temp": "30.0",
                  "Day_6_Min_temp": "20.0",
                  "Day_6_Forecast": "Partly cloudy sky",
                  "Day_7_Max_Temp": "30.0",
                  "Day_7_Min_temp": "20.0",
                  "Day_7_Forecast": "Partly cloudy sky"
                }
                """.formatted(stationId, stationId), ImdCityWeatherResponse.class);
    }

    private static <T> T readJson(ObjectMapper mapper, String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid embedded IMD mock JSON", e);
        }
    }

    static String meghalayaDistrictName(String objId) {
        return switch (objId) {
            case "1" -> "SOUTH GARO HILLS";
            case "2" -> "EAST KHASI HILLS";
            case "3" -> "WEST KHASI HILLS";
            case "4" -> "RI BHOI";
            case "5" -> "EAST JAINTIA HILLS";
            case "6" -> "WEST JAINTIA HILLS";
            case "7" -> "NORTH GARO HILLS";
            case "575" -> "EAST GARO HILLS";
            case "672" -> "WEST GARO HILLS";
            case "673" -> "SOUTH WEST KHASI HILLS";
            case "674" -> "SOUTH WEST GARO HILLS";
            default -> "DISTRICT " + objId;
        };
    }

    private static String nowcastColor(String objId) {
        return switch (objId) {
            case "1" -> "2";
            case "2" -> "3";
            case "3" -> "4";
            case "4" -> "1";
            default -> "2";
        };
    }

    private static String rainfallDailyCategory(String objId) {
        return switch (objId) {
            case "1" -> "NR";
            case "2" -> "E";
            case "3" -> "LE";
            case "4" -> "N";
            case "5" -> "D";
            case "6" -> "LD";
            case "7" -> "NR";
            case "575" -> "E";
            case "672" -> "N";
            case "673" -> "LE";
            case "674" -> "D";
            default -> "N";
        };
    }

    private static String rainfallDailyActual(String objId) {
        return switch (objId) {
            case "1" -> "0.00";
            case "2" -> "8.20";
            case "3" -> "15.40";
            case "4" -> "2.10";
            case "5" -> "1.20";
            case "6" -> "0.50";
            case "7" -> "0.00";
            case "575" -> "6.80";
            case "672" -> "3.30";
            case "673" -> "18.20";
            case "674" -> "4.50";
            default -> "3.50";
        };
    }

    private static String rainfallDailyDeparture(String objId) {
        return switch (objId) {
            case "1" -> "-100%";
            case "2" -> "35%";
            case "3" -> "65%";
            case "4" -> "5%";
            case "5" -> "-25%";
            case "6" -> "-70%";
            case "7" -> "-100%";
            case "575" -> "28%";
            case "672" -> "10%";
            case "673" -> "72%";
            case "674" -> "-18%";
            default -> "5%";
        };
    }

    private static String[] warningDayCodes(String objId) {
        return switch (objId) {
            case "1" -> new String[]{"1", "1", "2", "1", "1"};
            case "2" -> new String[]{"2", "2", "1", "1", "1"};
            case "3" -> new String[]{"4", "16", "2", "1", "1"};
            case "4" -> new String[]{"16", "2", "2", "1", "1"};
            case "5" -> new String[]{"9", "9", "1", "1", "1"};
            case "6" -> new String[]{"15", "1", "1", "1", "1"};
            case "7" -> new String[]{"1", "1", "1", "1", "1"};
            case "575" -> new String[]{"2", "2", "4", "1", "1"};
            case "672" -> new String[]{"1", "1", "1", "1", "1"};
            case "673" -> new String[]{"4", "4", "2", "2", "1"};
            case "674" -> new String[]{"2", "1", "1", "1", "1"};
            default -> new String[]{"1", "1", "1", "1", "1"};
        };
    }

    private static String[] warningDayColors(String objId) {
        return switch (objId) {
            case "1" -> new String[]{"4", "4", "2", "4", "4"};
            case "2" -> new String[]{"2", "2", "4", "4", "4"};
            case "3" -> new String[]{"1", "1", "2", "4", "4"};
            case "4" -> new String[]{"1", "2", "2", "4", "4"};
            case "5" -> new String[]{"3", "3", "4", "4", "4"};
            case "6" -> new String[]{"3", "4", "4", "4", "4"};
            case "7" -> new String[]{"4", "4", "4", "4", "4"};
            case "575" -> new String[]{"2", "2", "1", "4", "4"};
            case "672" -> new String[]{"4", "4", "4", "4", "4"};
            case "673" -> new String[]{"1", "1", "2", "2", "4"};
            case "674" -> new String[]{"2", "4", "4", "4", "4"};
            default -> new String[]{"4", "4", "4", "4", "4"};
        };
    }

    private static final String TURA_JSON = """
            {
              "Date": "2026-05-13",
              "Station_Code": "99489",
              "Station_Name": "TURA",
              "Today_Min_temp": "21.7",
              "Past_24_hrs_Rainfall": "53.60",
              "Relative_Humidity_at_0830": "90",
              "Sunset_time": "18:06",
              "Sunrise_time": "04:45",
              "Todays_Forecast_Max_Temp": "28.0",
              "Todays_Forecast_Min_temp": "21.0",
              "Todays_Forecast": "Generally cloudy sky with moderate rain",
              "Day_2_Max_Temp": "30.0",
              "Day_2_Min_temp": "21.0",
              "Day_2_Forecast": "Generally cloudy sky with moderate rain",
              "Day_3_Max_Temp": "30.0",
              "Day_3_Min_temp": "21.0",
              "Day_3_Forecast": "Generally cloudy sky with moderate rain",
              "Day_4_Max_Temp": "30.0",
              "Day_4_Min_temp": "21.0",
              "Day_4_Forecast": "Generally cloudy sky with moderate rain",
              "Day_5_Max_Temp": "28.0",
              "Day_5_Min_temp": "22.0",
              "Day_5_Forecast": "Generally cloudy sky with moderate rain",
              "Day_6_Max_Temp": "29.0",
              "Day_6_Min_temp": "23.0",
              "Day_6_Forecast": "Generally cloudy sky with moderate rain",
              "Day_7_Max_Temp": "30.0",
              "Day_7_Min_temp": "24.0",
              "Day_7_Forecast": "Generally cloudy sky with moderate rain"
            }
            """;

    public static ImdAwsDataResponse awsStation(String stationId, ObjectMapper mapper) {
        LocalDate today = LocalDate.now();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ID", stationId);
        m.put("STATION", awsStationDisplayName(stationId));
        m.put("DISTRICT", awsDistrictUnderscore(stationId));
        m.put("STATE", "MEGHALAYA");
        m.put("DATE", today.toString());
        m.put("TIME", "01:30:00");
        m.put("CURR_TEMP", "22.0");
        m.put("MIN_TEMP", "20.0");
        m.put("MAX_TEMP", "28.0");
        m.put("WIND_DIRECTION", "110");
        m.put("WIND_SPEED", 3.7);
        m.put("MSLP", "1003.1");
        m.put("RH", "85");
        m.put("WEATHER_CODE", "65");
        m.put("NEBULOSITY", "0");
        m.put("Feel Like", "22.0");
        m.put("WEATHER_ICON", 308);
        m.put("WEATHER_MESSAGE", "Light to moderate rain");
        m.put("BACKGROUND", "rain.png");
        m.put("BACKGROUND_URL", "https://mausam.imd.gov.in/img/bg/rain.png");
        return mapper.convertValue(m, ImdAwsDataResponse.class);
    }

    private static String awsStationDisplayName(String id) {
        return switch (id) {
            case "55C18D42" -> "RONGARA";
            case "55E41E0E" -> "SOHRA (CHERRAPUNJEE)";
            case "55E438E2" -> "MINENG";
            case "55E440A0" -> "BYRNIHAT";
            case "55E44E72" -> "RESUBELPARA";
            case "55E453D6" -> "MAIRANG";
            case "A0A2556C" -> "WILLIAMNAGAR";
            case "A0A25BBE" -> "BAGHMARA";
            case "A0A260F6" -> "TURA";
            case "A0A27380" -> "SHILLONG";
            case "A0A27D52" -> "NONGSTOIN";
            case "A0A28304" -> "JOWAI";
            case "MEAMJ000" -> "AMPATI";
            case "MELAN000" -> "LABAN";
            case "MEMWK000" -> "MAWKYRWAT";
            case "MEMWR000" -> "MAWRYNGKNENG";
            case "MEMWY000" -> "MAWSYNRAM";
            case "MENEV000" -> "NEHU";
            case "MERES000" -> "RESUBELPARA";
            case "MEUMT000" -> "UMTREWDAM";
            default -> "STATION " + id;
        };
    }

    private static String awsDistrictUnderscore(String id) {
        return switch (id) {
            case "MEAMJ000" -> "SOUTH_WEST_GARO_HILLS";
            case "A0A2556C" -> "EAST_GARO_HILLS";
            case "A0A25BBE", "A0A260F6", "MERES000", "55E44E72" -> "WEST_GARO_HILLS";
            case "A0A27380", "MENEV000", "MELAN000" -> "EAST_KHASI_HILLS";
            case "A0A27D52" -> "WEST_KHASI_HILLS";
            case "A0A28304", "MEMWK000" -> "WEST_JAINTIA_HILLS";
            case "MEMWY000", "55E41E0E" -> "EAST_KHASI_HILLS";
            case "MEMWR000" -> "EAST_KHASI_HILLS";
            case "55E438E2", "55C18D42", "55E453D6" -> "SOUTH_GARO_HILLS";
            case "55E440A0" -> "RI_BHOI";
            case "MEUMT000" -> "RI_BHOI";
            default -> "MEGHALAYA";
        };
    }

    private static final String SHILLONG_JSON = """
            {
              "Date": "2026-05-13",
              "Station_Code": "42516",
              "Station_Name": "SHILLONG",
              "Today_Max_temp": "24.2",
              "Today_Min_temp": "16.1",
              "Past_24_hrs_Rainfall": "12.40",
              "Relative_Humidity_at_0830": "88",
              "Sunset_time": "18:10",
              "Sunrise_time": "04:38",
              "Todays_Forecast_Max_Temp": "25.0",
              "Todays_Forecast_Min_temp": "16.0",
              "Todays_Forecast": "Generally cloudy sky with light rain",
              "Day_2_Max_Temp": "26.0",
              "Day_2_Min_temp": "16.0",
              "Day_2_Forecast": "Partly cloudy sky",
              "Day_3_Max_Temp": "27.0",
              "Day_3_Min_temp": "17.0",
              "Day_3_Forecast": "Generally cloudy sky",
              "Day_4_Max_Temp": "26.0",
              "Day_4_Min_temp": "17.0",
              "Day_4_Forecast": "Thunderstorm with rain",
              "Day_5_Max_Temp": "25.0",
              "Day_5_Min_temp": "17.0",
              "Day_5_Forecast": "Fog in the morning",
              "Day_6_Max_Temp": "24.0",
              "Day_6_Min_temp": "16.0",
              "Day_6_Forecast": "Mainly clear sky",
              "Day_7_Max_Temp": "25.0",
              "Day_7_Min_temp": "15.0",
              "Day_7_Forecast": "Sunny intervals"
            }
            """;
}
