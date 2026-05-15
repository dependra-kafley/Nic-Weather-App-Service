package com.example.weathermap.service;

import com.example.weathermap.dto.ImdAwsDataGroupedResponse;
import com.example.weathermap.dto.ImdAwsDataResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AwsDataService {

    private final AwsDataApiClient apiClient;

    // Hardcoded Categories based on the Requirement mapping
    private static final List<String> ORG_IDS = List.of("A0A2556C", "A0A260F6");
    private static final List<String> AWS_IDS = List.of(
            "MEAMJ000", "MEMWY000", "A0A28304", "A0A27380",
            "MEMWK000", "A0A27D52", "A0A25BBE", "MERES000"
    );
    private static final List<String> ARG_IDS = List.of(
            "55E438E2", "55C18D42", "55E41E0E", "MELAN000",
            "MEUMT000", "MEMWR000", "55E440A0", "MENEV000",
            "55E453D6", "55E44E72"
    );

    public AwsDataService(AwsDataApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ImdAwsDataGroupedResponse fetchGroupedAwsDataFromImd() {
        List<ImdAwsDataResponse> allData = apiClient.fetchAllConfiguredStations();

        List<ImdAwsDataResponse> orgList = new ArrayList<>();
        List<ImdAwsDataResponse> awsList = new ArrayList<>();
        List<ImdAwsDataResponse> argList = new ArrayList<>();

        for (ImdAwsDataResponse data : allData) {
            if (data == null || data.id() == null) continue;
            String id = data.id();
            
            if (ORG_IDS.contains(id)) {
                orgList.add(data);
            } else if (AWS_IDS.contains(id)) {
                awsList.add(data);
            } else if (ARG_IDS.contains(id)) {
                argList.add(data);
            } else {
                // Default to ARG if unknown
                argList.add(data);
            }
        }

        return new ImdAwsDataGroupedResponse(orgList, awsList, argList);
    }
}
