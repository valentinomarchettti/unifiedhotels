package com.challenge.unifiedhotels.service;

import com.challenge.unifiedhotels.api.hotels.dto.ApiWarning;
import com.challenge.unifiedhotels.api.hotels.dto.HotelSearchResponse;
import com.challenge.unifiedhotels.domain.StandardHotel;
import com.challenge.unifiedhotels.domain.enums.ProviderName;
import com.challenge.unifiedhotels.providers.geocoding.GeoapifyGeocodingClient;
import com.challenge.unifiedhotels.providers.places.geoapify.GeoapifyPlacesClient;
import com.challenge.unifiedhotels.providers.places.geoapify.GeoapifyPlacesMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class UnifiedHotelSearchService {

    private final GeoapifyGeocodingClient geocodingClient;
    private final GeoapifyPlacesClient placesClient;
    private final GeoapifyPlacesMapper mapper = new GeoapifyPlacesMapper();

    public UnifiedHotelSearchService(GeoapifyGeocodingClient geocodingClient,
                                     GeoapifyPlacesClient placesClient) {
        this.geocodingClient = geocodingClient;
        this.placesClient = placesClient;
    }

    public Mono<HotelSearchResponse> search(String city, double radiusKm, int limit) {
        int radiusMeters = (int) Math.round(radiusKm * 1000);

        return geocodingClient.geocodeCity(city)
                .flatMap(center ->
                        placesClient.searchHotels(center, radiusMeters, limit)
                                .map(resp -> {
                                    List<StandardHotel> results = (resp.features() == null)
                                            ? List.of()
                                            : resp.features().stream().map(mapper::toStandardHotel).toList();

                                    return new HotelSearchResponse(results, List.of());
                                })
                                .onErrorResume(ex -> Mono.just(
                                        new HotelSearchResponse(
                                                List.of(),
                                                List.of(new ApiWarning(
                                                        ProviderName.GEOAPIFY,
                                                        "PLACES_PROVIDER_FAILED",
                                                        ex.getMessage()
                                                ))
                                        )
                                ))
                );
    }
}
