package com.example.weathermap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ImdDistrictWarningResponse(
        @JsonProperty("Obj_id") String objId,
        @JsonProperty("Date") String date,
        @JsonProperty("UTC") String utc,
        @JsonProperty("District") String district,
        @JsonProperty("Day_1") String day1,
        @JsonProperty("Day_2") String day2,
        @JsonProperty("Day_3") String day3,
        @JsonProperty("Day_4") String day4,
        @JsonProperty("Day_5") String day5,
        @JsonProperty("Day1_Color") String day1Color,
        @JsonProperty("Day2_Color") String day2Color,
        @JsonProperty("Day3_Color") String day3Color,
        @JsonProperty("Day4_Color") String day4Color,
        @JsonProperty("Day5_Color") String day5Color
) {
}
