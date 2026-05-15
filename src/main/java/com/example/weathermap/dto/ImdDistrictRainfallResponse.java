package com.example.weathermap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ImdDistrictRainfallResponse(
        @JsonProperty("OBJ_ID") String objId,
        @JsonProperty("District") String district,
        @JsonProperty("Date") String date,
        @JsonProperty("Daily Actual") String dailyActual,
        @JsonProperty("Daily Normal") String dailyNormal,
        @JsonProperty("Daily Departure Per") String dailyDeparturePer,
        @JsonProperty("Daily Category") String dailyCategory,
        @JsonProperty("Week Date") String weekDate,
        @JsonProperty("Weekly Actual") String weeklyActual,
        @JsonProperty("Weekly Normal") String weeklyNormal,
        @JsonProperty("Weekly Departure Per") String weeklyDeparturePer,
        @JsonProperty("Weekly Category") String weeklyCategory,
        @JsonProperty("Cumulative Date") String cumulativeDate,
        @JsonProperty("Cumulative Actual") String cumulativeActual,
        @JsonProperty("Cumulative Normal") String cumulativeNormal,
        @JsonProperty("Cumulative Departure Per") String cumulativeDeparturePer,
        @JsonProperty("Cumulative Category") String cumulativeCategory,
        @JsonProperty("Monthly Date") String monthlyDate,
        @JsonProperty("Monthly Actual") String monthlyActual,
        @JsonProperty("Monthly Normal") String monthlyNormal,
        @JsonProperty("Monthly Departure Per") String monthlyDeparturePer,
        @JsonProperty("Monthly Category") String monthlyCategory
) {
}
