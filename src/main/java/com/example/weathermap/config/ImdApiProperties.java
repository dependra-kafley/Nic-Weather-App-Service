package com.example.weathermap.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "weather.imd")
public record ImdApiProperties(
        Duration connectTimeout,
        Duration readTimeout,
        @NotEmpty List<String> meghalayaDistrictObjIds,
        @Valid Endpoint nowcast,
        @Valid Endpoint rainfall,
        @Valid Endpoint districtWarning,
        @Valid CityWeather city,
        @Valid AwsData awsData
) {

    public static final List<String> DEFAULT_MEGHALAYA_DISTRICT_OBJ_IDS = List.of(
            "1", "2", "3", "4", "5", "6", "7", "575", "672", "673", "674"
    );

    public ImdApiProperties {
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(15);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(45);
        }
        if (meghalayaDistrictObjIds == null || meghalayaDistrictObjIds.isEmpty()) {
            meghalayaDistrictObjIds = DEFAULT_MEGHALAYA_DISTRICT_OBJ_IDS;
        } else {
            meghalayaDistrictObjIds = List.copyOf(meghalayaDistrictObjIds);
        }
        if (awsData == null) {
            awsData = new AwsData(
                    "https://api.imd.gov.in/api/v1/aws_data",
                    false,
                    AwsData.DEFAULT_STATION_IDS
            );
        }
    }

    public record Endpoint(
            @NotBlank String baseUrl,
            boolean mock
    ) {
    }

    public record CityWeather(
            @NotBlank String baseUrl,
            boolean mock,
            @NotEmpty List<String> stationIds
    ) {

        public CityWeather {
            if (stationIds == null || stationIds.isEmpty()) {
                stationIds = List.of("42516", "99489");
            } else {
                stationIds = List.copyOf(stationIds);
            }
        }
    }

    public record AwsData(
            @NotBlank String baseUrl,
            boolean mock,
            @NotEmpty List<String> stationIds
    ) {

        public static final List<String> DEFAULT_STATION_IDS = List.of(
                "A0A2556C", "A0A260F6",
                "MEAMJ000", "MEMWY000", "A0A28304", "A0A27380", "MEMWK000", "A0A27D52", "A0A25BBE", "MERES000",
                "55E438E2", "55C18D42", "55E41E0E", "MELAN000", "MEUMT000", "MEMWR000", "55E440A0", "MENEV000",
                "55E453D6", "55E44E72"
        );

        public AwsData {
            if (stationIds == null || stationIds.isEmpty()) {
                stationIds = DEFAULT_STATION_IDS;
            } else {
                stationIds = List.copyOf(stationIds);
            }
        }
    }
}
