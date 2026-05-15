package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdAwsDataResponse;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AwsDataApiClient {

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;

    public AwsDataApiClient(ImdApiProperties imdApiProperties, ImdHttpFetcher httpFetcher) {
        this.imdApiProperties = imdApiProperties;
        this.httpFetcher = httpFetcher;
    }

    public Optional<ImdAwsDataResponse> fetchById(String stationId) {
        if (imdApiProperties.awsData().mock()) {
            return Optional.empty(); // No mock implemented yet
        }
        return httpFetcher.fetch(
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
