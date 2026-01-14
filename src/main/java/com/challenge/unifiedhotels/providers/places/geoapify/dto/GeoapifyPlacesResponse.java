package com.challenge.unifiedhotels.providers.places.geoapify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeoapifyPlacesResponse(
        List<Feature> features
) {
    public record Feature(
            Properties properties,
            Geometry geometry
    ) {}

    public record Geometry(String type, List<Double> coordinates) {
        // GeoJSON Point: [lon, lat]
    }

    public record Properties(
            String name,
            String country,
            String city,
            String street,
            String housenumber,
            String postcode,
            Double lat,
            Double lon,
            Double distance, // meters
            @JsonProperty("place_id") String placeId
    ) {}
}
