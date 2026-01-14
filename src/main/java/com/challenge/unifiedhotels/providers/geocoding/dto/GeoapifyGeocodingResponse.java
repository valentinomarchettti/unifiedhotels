package com.challenge.unifiedhotels.providers.geocoding.dto;

import java.util.List;

public record GeoapifyGeocodingResponse(List<Result> results) {
    public record Result(Double lat, Double lon) {}
}