package com.example.weathermap.util;

/**
 * Normalizes IMD district names to the same slug used by the Meghalaya SVG map in {@code map.js}.
 */
public final class DistrictNameNormalizer {

    private DistrictNameNormalizer() {
    }

    public static String toDistrictKey(String districtName) {
        if (districtName == null || districtName.isBlank()) {
            return "";
        }
        String normalized = districtName.trim().replaceAll("_+", " ");
        normalized = normalized.replaceAll("[^a-zA-Z0-9]+", "_");
        normalized = normalized.replaceAll("^_+", "").replaceAll("_+$", "");
        return normalized.toUpperCase();
    }
}
