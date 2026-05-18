package com.example.weathermap.service;

import com.example.weathermap.config.ImdApiProperties;
import com.example.weathermap.dto.ImdDistrictWarningResponse;
import com.example.weathermap.service.imd.ImdApiCallAudit;
import com.example.weathermap.service.imd.ImdHttpFetcher;
import com.example.weathermap.service.imd.ImdMockResponses;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ImdDistrictWarningApiClient {

    private static final String API_NAME = "district-warning";

    private final ImdApiProperties imdApiProperties;
    private final ImdHttpFetcher httpFetcher;
    private final ImdApiCallAudit apiCallAudit;

    public ImdDistrictWarningApiClient(
            ImdApiProperties imdApiProperties,
            ImdHttpFetcher httpFetcher,
            ImdApiCallAudit apiCallAudit
    ) {
        this.imdApiProperties = imdApiProperties;
        this.httpFetcher = httpFetcher;
        this.apiCallAudit = apiCallAudit;
    }

    public Optional<ImdDistrictWarningResponse> fetchByObjId(String objId) {
        if (imdApiProperties.districtWarning().mock()) {
            apiCallAudit.callStarted(API_NAME, objId, true);
            apiCallAudit.callSucceeded(API_NAME, objId, true);
            return Optional.of(ImdMockResponses.districtWarning(objId));
        }
        return httpFetcher.fetch(
                API_NAME,
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
