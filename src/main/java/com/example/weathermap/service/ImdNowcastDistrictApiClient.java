package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdNowcastDistrictResponse;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import com.example.weathermap.service.imd.ImdMockResponses;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ImdNowcastDistrictApiClient {

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;
    private final ObjectMapper objectMapper;

    public ImdNowcastDistrictApiClient(
            ImdApiProperties imdApiProperties,
            ImdHttpFetcher httpFetcher,
            ObjectMapper objectMapper
    ) {
        this.imdApiProperties = imdApiProperties;
        this.httpFetcher = httpFetcher;
        this.objectMapper = objectMapper;
    }

    public Optional<ImdNowcastDistrictResponse> fetchByObjId(String objId) {
        if (imdApiProperties.nowcast().mock()) {
            return Optional.of(ImdMockResponses.nowcast(objId, objectMapper));
        }
        return httpFetcher.fetch(
                imdApiProperties.nowcast().baseUrl(),
                "id",
                objId,
                ImdNowcastDistrictResponse.class
        );
    }

    public List<ImdNowcastDistrictResponse> fetchAllConfiguredDistricts() {
        return imdApiProperties.meghalayaDistrictObjIds().stream()
                .map(this::fetchByObjId)
                .flatMap(Optional::stream)
                .toList();
    }
}
