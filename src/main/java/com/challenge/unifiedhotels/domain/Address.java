package com.challenge.unifiedhotels.domain;


public record Address(
        String country,
        String city,
        String street,
        String postalCode
) {}