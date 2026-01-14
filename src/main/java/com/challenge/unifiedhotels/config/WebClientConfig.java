package com.challenge.unifiedhotels.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient geoapifyWebClient(WebClient.Builder builder, GeoapifyProperties props) {
        return builder
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(addApiKey(props.getApiKey()))
                .build();
    }

    private static ExchangeFilterFunction addApiKey(String apiKey) {
        return (request, next) -> {
            URI newUri = UriComponentsBuilder.fromUri(request.url())
                    .queryParam("apiKey", apiKey)
                    .build(true)
                    .toUri();

            ClientRequest newRequest = ClientRequest.from(request)
                    .url(newUri)
                    .build();

            return next.exchange(newRequest);
        };
    }
}
