package com.challenge.unifiedhotels.domain;

import com.challenge.unifiedhotels.domain.enums.Amenity;
import com.challenge.unifiedhotels.domain.enums.PlaceType;

import java.util.List;
import java.util.Set;

public record StandardHotel(
        String id,                       // "std_..."
        String name,
        PlaceType type,                  // HOTEL, HOSTEL, etc
        Address address,                 // puede ser null
        GeoPoint geo,                    // tu record existente
        Rating rating,                   // puede ser null
        Set<Amenity> amenities,          // puede ser null o vacío
        Double distanceKm,               // se calcula luego
        Double score,                    // se calcula luego
        List<SourceReference> sourceReferences
) {}
