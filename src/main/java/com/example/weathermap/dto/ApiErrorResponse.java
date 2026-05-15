package com.example.weathermap.dto;

import java.time.Instant;

public record ApiErrorResponse(
        Instant timestamp,
        String error,
        String path
) {
}
