package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdDistrictWarningResponse;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import com.example.weathermap.service.imd.ImdMockResponses;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ImdDistrictWarningApiClient {

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;

    public ImdDistrictWarningApiClient(ImdApiProperties imdApiProperties, ImdHttpFetcher httpFetcher) {
        this.imdApiProperties = imdApiProperties;
        this.httpFetcher = httpFetcher;
    }

    public Optional<ImdDistrictWarningResponse> fetchByObjId(String objId) {
        if (imdApiProperties.districtWarning().mock()) {
            return Optional.of(ImdMockResponses.districtWarning(objId));
        }
        return httpFetcher.fetch(
                imdApiProperties.districtWarning().baseUrl(),
                "id",
                objId,
                ImdDistrictWarningResponse.class
        );
    }

    public List<ImdDistrictWarningResponse> fetchAllConfiguredDistricts() {
        return imdApiProperties.meghalayaDistrictObjIds().stream()
                .map(this::fetchByObjId)
                .flatMap(Optional::stream)
                .toList();
    }
}
