package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdDistrictRainfallResponse;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import com.example.weathermap.service.imd.ImdMockResponses;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ImdDistrictRainfallApiClient {

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;

    public ImdDistrictRainfallApiClient(ImdApiProperties imdApiProperties, ImdHttpFetcher httpFetcher) {
        this.imdApiProperties = imdApiProperties;
        this.httpFetcher = httpFetcher;
    }

    public Optional<ImdDistrictRainfallResponse> fetchByObjId(String objId) {
        if (imdApiProperties.rainfall().mock()) {
            return Optional.of(ImdMockResponses.rainfall(objId));
        }
        return httpFetcher.fetch(
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
