package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdCityWeatherResponse;
import com.example.weathermap.service.imd.ImdApiCallAudit;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import com.example.weathermap.service.imd.ImdMockResponses;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ImdCityWeatherApiClient {

    private static final String API_NAME = "city-weather";

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;
    private final ObjectMapper objectMapper;
    private final ImdApiCallAudit apiCallAudit;

    public ImdCityWeatherApiClient(
            ImdApiProperties imdApiProperties,
            ImdHttpFetcher httpFetcher,
            ObjectMapper objectMapper,
            ImdApiCallAudit apiCallAudit
    ) {
        this.imdApiProperties = imdApiProperties;
        this.httpFetcher = httpFetcher;
        this.objectMapper = objectMapper;
        this.apiCallAudit = apiCallAudit;
    }

    public Optional<ImdCityWeatherResponse> fetchByStationId(String stationId) {
        if (imdApiProperties.city().mock()) {
            apiCallAudit.callStarted(API_NAME, stationId, true);
            apiCallAudit.callSucceeded(API_NAME, stationId, true);
            return Optional.of(ImdMockResponses.city(stationId, objectMapper));
        }
        return httpFetcher.fetch(
                API_NAME,
                imdApiProperties.city().baseUrl(),
                "id",
                stationId,
                ImdCityWeatherResponse.class
        );
    }

    public List<ImdCityWeatherResponse> fetchAllConfiguredStations() {
        return imdApiProperties.city().stationIds().stream()
                .map(this::fetchByStationId)
                .flatMap(Optional::stream)
                .toList();
    }
}
