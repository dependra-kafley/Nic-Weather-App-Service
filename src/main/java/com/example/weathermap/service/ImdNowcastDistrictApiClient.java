package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdNowcastDistrictResponse;
import com.example.weathermap.service.imd.ImdApiCallAudit;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import com.example.weathermap.service.imd.ImdMockResponses;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ImdNowcastDistrictApiClient {

    private static final String API_NAME = "nowcast-district";

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;
    private final ObjectMapper objectMapper;
    private final ImdApiCallAudit apiCallAudit;

    public ImdNowcastDistrictApiClient(
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

    public Optional<ImdNowcastDistrictResponse> fetchByObjId(String objId) {
        if (imdApiProperties.nowcast().mock()) {
            apiCallAudit.callStarted(API_NAME, objId, true);
            apiCallAudit.callSucceeded(API_NAME, objId, true);
            return Optional.of(ImdMockResponses.nowcast(objId, objectMapper));
        }
        return httpFetcher.fetch(
                API_NAME,
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
