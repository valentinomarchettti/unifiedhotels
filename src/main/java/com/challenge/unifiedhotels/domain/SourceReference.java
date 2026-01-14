package com.challenge.unifiedhotels.domain;

import com.challenge.unifiedhotels.domain.enums.ProviderName;

public record SourceReference(
        ProviderName provider,
        String externalId
) {}