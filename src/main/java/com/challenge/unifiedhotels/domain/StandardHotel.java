package com.challenge.unifiedhotels.domain;

import com.challenge.unifiedhotels.domain.enums.Amenity;
import com.challenge.unifiedhotels.domain.enums.PlaceType;

import java.util.List;
import java.util.Set;

public record StandardHotel(
        String id,
        String name,
        PlaceType type,
        Address address,
        GeoPoint geo,
        Rating rating,
        Set<Amenity> amenities,
        Double distanceKm,
        Double score,
        List<SourceReference> sourceReferences
) {}
