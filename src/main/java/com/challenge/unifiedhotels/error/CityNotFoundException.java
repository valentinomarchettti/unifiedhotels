package com.challenge.unifiedhotels.error;

public class CityNotFoundException extends RuntimeException {
    public CityNotFoundException(String city) {
        super("No se pudo resolver la ciudad '" + city + "'");
    }
}