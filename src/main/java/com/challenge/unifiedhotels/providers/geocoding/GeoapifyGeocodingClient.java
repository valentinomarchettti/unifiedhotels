package com.challenge.unifiedhotels.providers.geocoding;

import com.challenge.unifiedhotels.config.GeoapifyProperties;
import com.challenge.unifiedhotels.domain.GeoPoint;
import com.challenge.unifiedhotels.error.CityNotFoundException;
import com.challenge.unifiedhotels.providers.geocoding.dto.GeoapifyGeocodingResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class GeoapifyGeocodingClient {

    private final WebClient geoapifyWebClient;
    private final GeoapifyProperties props;

    public GeoapifyGeocodingClient(WebClient geoapifyWebClient, GeoapifyProperties props) {
        this.geoapifyWebClient = geoapifyWebClient;
        this.props = props;
    }

    public Mono<GeoPoint> geocodeCity(String city) {
        return geoapifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/geocode/search")
                        .queryParam("text", city)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .bodyToMono(GeoapifyGeocodingResponse.class)
                .timeout(Duration.ofMillis(props.getTimeoutMs()))
                .flatMap(resp -> {
                    if (resp == null || resp.results() == null || resp.results().isEmpty()) {
                        return Mono.error(new CityNotFoundException(city));
                    }
                    var r = resp.results().get(0);
                    if (r.lat() == null || r.lon() == null) {
                        return Mono.error(new CityNotFoundException(city));
                    }
                    return Mono.just(new GeoPoint(r.lat(), r.lon()));
                });
    }

    public Mono<String> rawSearch(String text) {
        return geoapifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/geocode/search")
                        .queryParam("text", text)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }
}