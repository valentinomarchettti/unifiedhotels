package com.challenge.unifiedhotels.api.hotels.dto;

import com.challenge.unifiedhotels.domain.enums.ProviderName;

public record ApiWarning(
        ProviderName provider,
        String code,
        String message
) {}
