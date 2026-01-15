package com.challenge.unifiedhotels.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opentripmap")
public class OpenTripMapProperties {

    private String baseUrl;   // ej: https://api.opentripmap.com/0.1/en
    private String apiKey;
    private long timeoutMs = 2500;

    private String kinds = "accomodations";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }

    public String getKinds() { return kinds; }
    public void setKinds(String kinds) { this.kinds = kinds; }
}
