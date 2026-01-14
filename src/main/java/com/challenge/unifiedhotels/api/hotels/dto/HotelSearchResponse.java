package com.challenge.unifiedhotels.api.hotels.dto;

import com.challenge.unifiedhotels.domain.StandardHotel;

import java.util.List;

public record HotelSearchResponse(
        List<StandardHotel> results,
        List<ApiWarning> warnings
) {}