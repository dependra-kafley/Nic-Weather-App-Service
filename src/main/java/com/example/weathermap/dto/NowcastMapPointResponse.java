package com.example.weathermap.dto;

/**
 * Nowcast payload for the district SVG map. {@code districtKey} matches {@code slugify(svgPathId)} /
 * {@code slugify(State_District)} on the client.
 */
public record NowcastMapPointResponse(
        String districtKey,
        String objId,
        String stateDistrict,
        String date,
        String color,
        String colorHex,
        String toi,
        String vupto,
        String message,
        String nowcastSummary
) {
}
