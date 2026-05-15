package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdCityWeatherResponse;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import com.example.weathermap.service.imd.ImdMockResponses;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ImdCityWeatherApiClient {

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;
    private final ObjectMapper objectMapper;

    public ImdCityWeatherApiClient(
            ImdApiProperties imdApiProperties,
            ImdHttpFetcher httpFetcher,
            ObjectMapper objectMapper
    ) {
        this.imdApiProperties = imdApiProperties;
        this.httpFetcher = httpFetcher;
        this.objectMapper = objectMapper;
    }

    public Optional<ImdCityWeatherResponse> fetchByStationId(String stationId) {
        if (imdApiProperties.city().mock()) {
            return Optional.of(ImdMockResponses.city(stationId, objectMapper));
        }
        return httpFetcher.fetch(
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
