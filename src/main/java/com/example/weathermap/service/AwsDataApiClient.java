package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdAwsDataResponse;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import com.example.weathermap.service.imd.ImdMockResponses;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AwsDataApiClient {

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;
    private final ObjectMapper objectMapper;

    public AwsDataApiClient(
            ImdApiProperties imdApiProperties,
            ImdHttpFetcher httpFetcher,
            ObjectMapper objectMapper
    ) {
        this.imdApiProperties = imdApiProperties;
        this.httpFetcher = httpFetcher;
        this.objectMapper = objectMapper;
    }

    public Optional<ImdAwsDataResponse> fetchById(String stationId) {
        if (imdApiProperties.awsData().mock()) {
            return Optional.of(ImdMockResponses.awsStation(stationId, objectMapper));
        }
        return httpFetcher.fetch(
                "aws-data-api",
                imdApiProperties.awsData().baseUrl(),
                "id",
                stationId,
                ImdAwsDataResponse.class
        );
    }

    public List<ImdAwsDataResponse> fetchAllConfiguredStations() {
        return imdApiProperties.awsData().stationIds().stream()
                .map(this::fetchById)
                .flatMap(Optional::stream)
                .toList();
    }
}
