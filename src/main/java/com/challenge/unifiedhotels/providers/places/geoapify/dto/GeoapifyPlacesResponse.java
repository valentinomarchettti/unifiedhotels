package com.challenge.unifiedhotels.providers.places.geoapify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoapifyPlacesResponse(
        @JsonProperty("features") List<Feature> features
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Feature(
            @JsonProperty("properties") Properties properties,
            @JsonProperty("geometry") Geometry geometry
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(
            @JsonProperty("coordinates") List<Double> coordinates
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Properties(
            String name,
            String country,
            @JsonProperty("country_code") String countryCode,
            String city,
            String street,
            String housenumber,
            String postcode,
            Double lat,
            Double lon,
            Double distance,
            @JsonProperty("place_id") String placeId,
            List<String> categories,
            Map<String, Object> facilities,
            Datasource datasource
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Datasource(
            String sourcename,
            Map<String, Object> raw
    ) {}
}
