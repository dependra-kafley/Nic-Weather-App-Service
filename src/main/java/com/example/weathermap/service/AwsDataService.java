package com.example.weathermap.service;

import com.example.weathermap.dto.ImdAwsDataGroupedResponse;
import com.example.weathermap.dto.ImdAwsDataResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AwsDataService {

    private final AwsDataApiClient apiClient;

    /**
     * Meghalaya IWS-style grouping (ORG left table, AWS blue, ARG green).
     * Two Resubelpara IDs: MERES000 → AWS, 55E44E72 → ARG per IWS layout.
     */
    private static final List<String> ORG_IDS = List.of("A0A2556C", "A0A260F6");

    private static final List<String> AWS_IDS = List.of(
            "MEAMJ000",
            "MEMWY000",
            "A0A28304",
            "MERES000",
            "A0A25BBE",
            "A0A27380",
            "MEMWK000",
            "A0A27D52"
    );

    private static final List<String> ARG_IDS = List.of(
            "55E438E2",
            "55C18D42",
            "55E41E0E",
            "55E44E72",
            "MELAN000",
            "MEUMT000",
            "MEMWR000",
            "55E440A0",
            "MENEV000",
            "55E453D6"
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
            if (data == null || data.id() == null) {
                continue;
            }
            String id = data.id();
            if (ORG_IDS.contains(id)) {
                orgList.add(data);
            } else if (AWS_IDS.contains(id)) {
                awsList.add(data);
            } else if (ARG_IDS.contains(id)) {
                argList.add(data);
            } else {
                argList.add(data);
            }
        }

        return new ImdAwsDataGroupedResponse(orgList, awsList, argList);
    }
}
