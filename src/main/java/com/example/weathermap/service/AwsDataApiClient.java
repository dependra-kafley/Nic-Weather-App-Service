package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdAwsDataResponse;
import com.example.weathermap.service.imd.ImdApiCallAudit;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import com.example.weathermap.service.imd.ImdMockResponses;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AwsDataApiClient {

    private static final String API_NAME = "aws-data-api";

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;
    private final ObjectMapper objectMapper;
    private final ImdApiCallAudit apiCallAudit;

    public AwsDataApiClient(
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

    public Optional<ImdAwsDataResponse> fetchById(String stationId) {
        if (imdApiProperties.awsData().mock()) {
            apiCallAudit.callStarted(API_NAME, stationId, true);
            apiCallAudit.callSucceeded(API_NAME, stationId, true);
            return Optional.of(ImdMockResponses.awsStation(stationId, objectMapper));
        }
        return httpFetcher.fetch(
                API_NAME,
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
