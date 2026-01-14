package com.challenge.unifiedhotels.providers.places.geoapify;

import com.challenge.unifiedhotels.domain.Address;
import com.challenge.unifiedhotels.domain.GeoPoint;
import com.challenge.unifiedhotels.domain.SourceReference;
import com.challenge.unifiedhotels.domain.StandardHotel;
import com.challenge.unifiedhotels.domain.enums.PlaceType;
import com.challenge.unifiedhotels.domain.enums.ProviderName;
import com.challenge.unifiedhotels.providers.places.geoapify.dto.GeoapifyPlacesResponse;

import java.util.List;
import java.util.Set;

public class GeoapifyPlacesMapper {

    public StandardHotel toStandardHotel(GeoapifyPlacesResponse.Feature f) {
        var p = f.properties();

        Double lat = (p.lat() != null) ? p.lat() : (f.geometry() != null && f.geometry().coordinates() != null ? f.geometry().coordinates().get(1) : null);
        Double lon = (p.lon() != null) ? p.lon() : (f.geometry() != null && f.geometry().coordinates() != null ? f.geometry().coordinates().get(0) : null);

        GeoPoint geo = (lat != null && lon != null) ? new GeoPoint(lat, lon) : null;

        String street = joinStreet(p.street(), p.housenumber());

        Address address = (p.country() != null || p.city() != null || street != null || p.postcode() != null)
                ? new Address(p.country(), p.city(), street, p.postcode())
                : null;

        Double distanceKm = (p.distance() != null) ? (p.distance() / 1000.0) : null;

        // id estable: por ahora lo dejo simple (después lo “hashéas” como en el roadmap)
        String id = (p.placeId() != null) ? ("std_geoapify_" + p.placeId()) : null;

        return new StandardHotel(
                id,
                p.name(),
                PlaceType.HOTEL,
                address,
                geo,
                null,               // rating no siempre viene
                null,               // amenities por ahora
                distanceKm,
                null,               // score después
                List.of(new SourceReference(ProviderName.GEOAPIFY, p.placeId()))
        );
    }

    private static String joinStreet(String street, String housenumber) {
        if (street == null && housenumber == null) return null;
        if (street == null) return housenumber;
        if (housenumber == null) return street;
        return street + " " + housenumber;
    }
}
