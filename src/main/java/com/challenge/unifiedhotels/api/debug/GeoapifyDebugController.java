package com.challenge.unifiedhotels.api.debug;

import com.challenge.unifiedhotels.domain.GeoPoint;
import com.challenge.unifiedhotels.providers.geocoding.GeoapifyGeocodingClient;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Profile("dev")
@RestController
@RequestMapping("/api/v1/_debug")
public class GeoapifyDebugController {

    private final GeoapifyGeocodingClient client;

    public GeoapifyDebugController(GeoapifyGeocodingClient client) {
        this.client = client;
    }

    @GetMapping("/geocode")
    public Mono<GeoPoint> geocode(@RequestParam @NotBlank(message = "The 'city' parameter is required") String city) {
        return client.geocodeCity(city);
    }
}
