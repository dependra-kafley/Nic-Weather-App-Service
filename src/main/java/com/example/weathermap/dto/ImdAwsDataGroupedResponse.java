package com.example.weathermap.dto;

import java.util.List;

public record ImdAwsDataGroupedResponse(
        List<ImdAwsDataResponse> org,
        List<ImdAwsDataResponse> aws,
        List<ImdAwsDataResponse> arg
) {}
