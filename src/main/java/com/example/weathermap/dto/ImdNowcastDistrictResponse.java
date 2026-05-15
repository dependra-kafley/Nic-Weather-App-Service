package com.example.weathermap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * IMD district nowcast JSON from
 * {@code https://mausam.imd.gov.in/api/nowcast_district_api.php?id={obj_id}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ImdNowcastDistrictResponse(
        @JsonProperty("Obj_id") String objId,
        @JsonProperty("State_District") String stateDistrict,
        @JsonProperty("Date") LocalDate date,
        @JsonProperty("cat1") String cat1,
        @JsonProperty("cat2") String cat2,
        @JsonProperty("cat3") String cat3,
        @JsonProperty("cat4") String cat4,
        @JsonProperty("cat5") String cat5,
        @JsonProperty("cat6") String cat6,
        @JsonProperty("cat7") String cat7,
        @JsonProperty("cat8") String cat8,
        @JsonProperty("cat9") String cat9,
        @JsonProperty("cat10") String cat10,
        @JsonProperty("cat11") String cat11,
        @JsonProperty("cat12") String cat12,
        @JsonProperty("cat13") String cat13,
        @JsonProperty("cat14") String cat14,
        @JsonProperty("cat15") String cat15,
        @JsonProperty("cat16") String cat16,
        @JsonProperty("cat17") String cat17,
        @JsonProperty("cat18") String cat18,
        @JsonProperty("cat19") String cat19,
        String message,
        String toi,
        @JsonProperty("vupto") String vupto,
        String color
) {
}
