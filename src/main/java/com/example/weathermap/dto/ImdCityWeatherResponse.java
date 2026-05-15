package com.example.weathermap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ImdCityWeatherResponse(
        @JsonProperty("Date") String date,
        @JsonProperty("Station_Code") String stationCode,
        @JsonProperty("Station_Name") String stationName,
        @JsonProperty("Today_Max_temp") String todayMaxTemp,
        @JsonProperty("Today_Max_Departure_from_Normal") String todayMaxDepartureFromNormal,
        @JsonProperty("Previous_Day_Max_temp") String previousDayMaxTemp,
        @JsonProperty("Previous_Day_Max_Departure_from_Normal") String previousDayMaxDepartureFromNormal,
        @JsonProperty("Today_Min_temp") String todayMinTemp,
        @JsonProperty("Today_Min_Departure_from_Normal") String todayMinDepartureFromNormal,
        @JsonProperty("Past_24_hrs_Rainfall") String past24HrsRainfall,
        @JsonProperty("Relative_Humidity_at_0830") String relativeHumidityAt0830,
        @JsonProperty("Relative_Humidity_at_1730") String relativeHumidityAt1730,
        @JsonProperty("Previous_Day_Relative_Humidity_at_1730") String previousDayRelativeHumidityAt1730,
        @JsonProperty("Sunset_time") String sunsetTime,
        @JsonProperty("Sunrise_time") String sunriseTime,
        @JsonProperty("Moonset_time") String moonsetTime,
        @JsonProperty("Moonrise_time") String moonriseTime,
        @JsonProperty("Todays_Forecast_Max_Temp") String todaysForecastMaxTemp,
        @JsonProperty("Todays_Forecast_Min_temp") String todaysForecastMinTemp,
        @JsonProperty("Todays_Forecast") String todaysForecast,
        @JsonProperty("Day_2_Max_Temp") String day2MaxTemp,
        @JsonProperty("Day_2_Min_temp") String day2MinTemp,
        @JsonProperty("Day_2_Forecast") String day2Forecast,
        @JsonProperty("Day_3_Max_Temp") String day3MaxTemp,
        @JsonProperty("Day_3_Min_temp") String day3MinTemp,
        @JsonProperty("Day_3_Forecast") String day3Forecast,
        @JsonProperty("Day_4_Max_Temp") String day4MaxTemp,
        @JsonProperty("Day_4_Min_temp") String day4MinTemp,
        @JsonProperty("Day_4_Forecast") String day4Forecast,
        @JsonProperty("Day_5_Max_Temp") String day5MaxTemp,
        @JsonProperty("Day_5_Min_temp") String day5MinTemp,
        @JsonProperty("Day_5_Forecast") String day5Forecast,
        @JsonProperty("Day_6_Max_Temp") String day6MaxTemp,
        @JsonProperty("Day_6_Min_temp") String day6MinTemp,
        @JsonProperty("Day_6_Forecast") String day6Forecast,
        @JsonProperty("Day_7_Max_Temp") String day7MaxTemp,
        @JsonProperty("Day_7_Min_temp") String day7MinTemp,
        @JsonProperty("Day_7_Forecast") String day7Forecast,
        @JsonProperty("Latitude") String latitude,
        @JsonProperty("Longitude") String longitude
) {
}
