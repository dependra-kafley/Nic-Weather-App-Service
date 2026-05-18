package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdDistrictRainfallResponse;
import com.example.weathermap.service.imd.ImdApiCallAudit;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import com.example.weathermap.service.imd.ImdMockResponses;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ImdDistrictRainfallApiClient {

    private static final String API_NAME = "district-rainfall";

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;
    private final ImdApiCallAudit apiCallAudit;

    public ImdDistrictRainfallApiClient(
            ImdApiProperties imdApiProperties,
            ImdHttpFetcher httpFetcher,
            ImdApiCallAudit apiCallAudit
    ) {
        this.imdApiProperties = imdApiProperties;
        this.httpFetcher = httpFetcher;
        this.apiCallAudit = apiCallAudit;
    }

    public Optional<ImdDistrictRainfallResponse> fetchByObjId(String objId) {
        if (imdApiProperties.rainfall().mock()) {
            apiCallAudit.callStarted(API_NAME, objId, true);
            apiCallAudit.callSucceeded(API_NAME, objId, true);
            return Optional.of(ImdMockResponses.rainfall(objId));
        }
        return httpFetcher.fetch(
                API_NAME,
                imdApiProperties.rainfall().baseUrl(),
                "id",
                objId,
                ImdDistrictRainfallResponse.class
        );
    }

    public List<ImdDistrictRainfallResponse> fetchAllConfiguredDistricts() {
        return imdApiProperties.meghalayaDistrictObjIds().stream()
                .map(this::fetchByObjId)
                .flatMap(Optional::stream)
                .toList();
    }
}
