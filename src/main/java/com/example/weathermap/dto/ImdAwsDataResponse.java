package com.example.weathermap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImdAwsDataResponse(
        @JsonProperty("ID") String id,
        @JsonProperty("CALL_SIGN") String callSign,
        @JsonProperty("DISTRICT") String district,
        @JsonProperty("STATE") String state,
        @JsonProperty("STATION") String station,
        @JsonProperty("DATE") String date,
        @JsonProperty("TIME") String time,
        @JsonProperty("CURR_TEMP") String currTemp,
        @JsonProperty("DEW_POINT_TEMP") String dewPointTemp,
        @JsonProperty("RH") String rh,
        @JsonProperty("WIND_DIRECTION") String windDirection,
        @JsonProperty("WIND_SPEED") String windSpeed,
        @JsonProperty("MSLP") String mslp,
        @JsonProperty("MIN_TEMP") String minTemp,
        @JsonProperty("MAX_TEMP") String maxTemp,
        @JsonProperty("Latitude") String latitude,
        @JsonProperty("Longitude") String longitude,
        @JsonProperty("WEATHER_CODE") String weatherCode,
        @JsonProperty("NEBULOSITY") String nebulosity,
        @JsonProperty("Feel Like") String feelLike,
        @JsonProperty("WEATHER_ICON") String weatherIcon,
        @JsonProperty("WEATHER_MESSAGE") String weatherMessage,
        @JsonProperty("BACKGROUND") String background,
        @JsonProperty("BACKGROUND_URL") String backgroundUrl
) {}
