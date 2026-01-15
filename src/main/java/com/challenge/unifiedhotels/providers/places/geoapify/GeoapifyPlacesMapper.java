package com.challenge.unifiedhotels.providers.places.geoapify;

import com.challenge.unifiedhotels.domain.Address;
import com.challenge.unifiedhotels.domain.GeoPoint;
import com.challenge.unifiedhotels.domain.SourceReference;
import com.challenge.unifiedhotels.domain.StandardHotel;
import com.challenge.unifiedhotels.domain.enums.Amenity;
import com.challenge.unifiedhotels.domain.enums.PlaceType;
import com.challenge.unifiedhotels.domain.enums.ProviderName;
import com.challenge.unifiedhotels.providers.places.geoapify.dto.GeoapifyPlacesResponse;

import java.util.*;

public class GeoapifyPlacesMapper {

    public StandardHotel toStandardHotel(GeoapifyPlacesResponse.Feature f) {
        if (f == null || f.properties() == null) return null;

        GeoapifyPlacesResponse.Properties p = f.properties();

        String placeId = p.placeId();
        String stdId = (placeId != null && !placeId.isBlank())
                ? "std_geoapify_" + placeId
                : buildFallbackId(p, f);

        String name = (p.name() != null && !p.name().isBlank()) ? p.name() : "Unknown";

        List<String> categories = (p.categories() != null) ? p.categories() : List.of();

        PlaceType type = inferType(categories);

        Address address = buildAddress(p);

        GeoPoint geo = buildGeo(p, f.geometry());

        Double distanceKm = (p.distance() != null) ? p.distance() / 1000.0 : null;

        Map<String, Object> facilities = p.facilities();
        Map<String, Object> raw = (p.datasource() != null) ? p.datasource().raw() : null;

        Set<Amenity> amenities = inferAmenities(categories, facilities, raw);

        List<SourceReference> sources = (placeId != null && !placeId.isBlank())
                ? List.of(new SourceReference(ProviderName.GEOAPIFY, placeId))
                : List.of();

        return new StandardHotel(
                stdId,
                name,
                type,
                address,
                geo,
                null,
                amenities,
                distanceKm,
                null,
                sources
        );
    }

    private PlaceType inferType(List<String> categories) {
        if (categories == null) return PlaceType.OTHER;

        if (categories.contains("accommodation.hotel")) return PlaceType.HOTEL;
        if (categories.contains("accommodation.hostel")) return PlaceType.HOSTEL;
        if (categories.contains("accommodation.apartment") || categories.contains("accommodation.apartments"))
            return PlaceType.APARTMENT;
        if (categories.contains("accommodation.guest_house")) return PlaceType.GUEST_HOUSE;
        if (categories.contains("accommodation.resort")) return PlaceType.RESORT;

        return PlaceType.OTHER;
    }

    private Set<Amenity> inferAmenities(List<String> categories,
                                        Map<String, Object> facilities,
                                        Map<String, Object> raw) {
        if ((categories == null || categories.isEmpty())
                && (facilities == null || facilities.isEmpty())
                && (raw == null || raw.isEmpty())) {
            return Set.of();
        }

        EnumSet<Amenity> out = EnumSet.noneOf(Amenity.class);

        // --- CONDITIONS (vienen en properties.categories) ---
        // WIFI
        if (hasCondition(categories, "internet_access") || truthy(facilities, "internet_access") || truthy(raw, "internet_access")) {
            out.add(Amenity.WIFI);
        }

        // PET FRIENDLY (dogs / no-dogs)
        boolean noDogs = hasExact(categories, "no-dogs");
        boolean dogsOk = hasCondition(categories, "dogs") || truthy(raw, "dogs") || truthy(raw, "pets");
        if (!noDogs && dogsOk) {
            out.add(Amenity.PET_FRIENDLY);
        }

        // --- OSM TAGS / FACILITIES (solo si vienen) ---
        if (truthy(facilities, "air_conditioning") || truthy(raw, "air_conditioning")) out.add(Amenity.AIR_CONDITIONING);
        if (truthy(raw, "parking")) out.add(Amenity.PARKING);
        if (truthy(raw, "swimming_pool") || truthy(raw, "pool")) out.add(Amenity.POOL);
        if (truthy(raw, "fitness_centre") || truthy(raw, "gym")) out.add(Amenity.GYM);
        if (truthy(raw, "spa")) out.add(Amenity.SPA);
        if (hasCondition(categories, "catering.restaurant") || truthy(raw, "restaurant")) out.add(Amenity.RESTAURANT);
        if (truthy(raw, "breakfast")) out.add(Amenity.BREAKFAST);

        return out;
    }

    private boolean hasExact(List<String> categories, String key) {
        if (categories == null) return false;
        for (String c : categories) {
            if (key.equals(c)) return true;
        }
        return false;
    }

    private boolean hasCondition(List<String> categories, String parentOrExact) {
        if (categories == null) return false;
        String prefix = parentOrExact + ".";
        for (String c : categories) {
            if (c == null) continue;
            if (c.equals(parentOrExact) || c.startsWith(prefix)) return true;
        }
        return false;
    }

    private boolean truthy(Map<String, Object> map, String key) {
        if (map == null) return false;
        return truthy(map.get(key));
    }

    private boolean truthy(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;

        String s = v.toString().trim().toLowerCase();
        if (s.isEmpty()) return false;

        // Geoapify/OSM suelen usar "yes/no" y también valores tipo "wlan"
        return !(s.equals("no") || s.equals("false") || s.equals("0") || s.equals("none"));
    }

    private Address buildAddress(GeoapifyPlacesResponse.Properties p) {
        if (p == null) return null;

        String country = (p.countryCode() != null && !p.countryCode().isBlank())
                ? p.countryCode().toUpperCase()
                : p.country();

        String street = joinStreetAndNumber(p.street(), p.housenumber());

        boolean hasAny = (country != null && !country.isBlank())
                || (p.city() != null && !p.city().isBlank())
                || (street != null && !street.isBlank())
                || (p.postcode() != null && !p.postcode().isBlank());

        if (!hasAny) return null;

        return new Address(
                emptyToNull(country),
                emptyToNull(p.city()),
                emptyToNull(street),
                emptyToNull(p.postcode())
        );
    }

    private GeoPoint buildGeo(GeoapifyPlacesResponse.Properties p, GeoapifyPlacesResponse.Geometry g) {
        if (p != null && p.lat() != null && p.lon() != null) {
            return new GeoPoint(p.lat(), p.lon());
        }

        if (g != null && g.coordinates() != null && g.coordinates().size() >= 2) {
            Double lon = g.coordinates().get(0);
            Double lat = g.coordinates().get(1);
            if (lat != null && lon != null) return new GeoPoint(lat, lon);
        }

        return null;
    }

    private String joinStreetAndNumber(String street, String houseNumber) {
        String s = emptyToNull(street);
        String h = emptyToNull(houseNumber);
        if (s == null && h == null) return null;
        if (s == null) return h;
        if (h == null) return s;
        return s + " " + h;
    }

    private String emptyToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private String buildFallbackId(GeoapifyPlacesResponse.Properties p, GeoapifyPlacesResponse.Feature f) {
        GeoPoint geo = buildGeo(p, f.geometry());
        String key = (p.name() == null ? "" : p.name()) + ":" +
                (geo == null ? "" : (geo.lat() + ":" + geo.lon()));
        return "std_geoapify_fallback_" + Integer.toHexString(key.hashCode());
    }
}
