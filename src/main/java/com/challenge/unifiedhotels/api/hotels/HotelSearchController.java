package com.challenge.unifiedhotels.api.hotels;

import com.challenge.unifiedhotels.api.hotels.dto.HotelSearchResponse;
import com.challenge.unifiedhotels.service.UnifiedHotelSearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("/api/v1/hotels")
public class HotelSearchController {

    private final UnifiedHotelSearchService service;

    public HotelSearchController(UnifiedHotelSearchService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public Mono<HotelSearchResponse> search(
            @RequestParam @NotBlank String city,
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) double radiusKm,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit
    ) {
        return service.search(city, radiusKm, limit);
    }
}
