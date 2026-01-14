package com.challenge.unifiedhotels.providers.places.geoapify;

import com.challenge.unifiedhotels.config.GeoapifyProperties;
import com.challenge.unifiedhotels.domain.GeoPoint;
import com.challenge.unifiedhotels.providers.places.geoapify.dto.GeoapifyPlacesResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class GeoapifyPlacesClient {

    private final WebClient geoapifyWebClient;
    private final GeoapifyProperties props;

    public GeoapifyPlacesClient(WebClient geoapifyWebClient, GeoapifyProperties props) {
        this.geoapifyWebClient = geoapifyWebClient;
        this.props = props;
    }

    public Mono<GeoapifyPlacesResponse> searchHotels(GeoPoint center, int radiusMeters, int limit) {
        String categories = "accommodation.hotel";
        String filter = "circle:" + center.lon() + "," + center.lat() + "," + radiusMeters; // ojo: lon,lat

        return geoapifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/places")
                        .queryParam("categories", categories)
                        .queryParam("filter", filter)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .bodyToMono(GeoapifyPlacesResponse.class)
                .timeout(Duration.ofMillis(props.getTimeoutMs()));
    }
}
