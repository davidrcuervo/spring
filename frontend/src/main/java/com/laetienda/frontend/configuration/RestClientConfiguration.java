package com.laetienda.frontend.configuration;

import com.laetienda.lib.service.CustomRestClient;
import com.laetienda.lib.service.CustomRestClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfiguration {

    private final RestClient.Builder builder;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public RestClientConfiguration(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {
        this.builder = builder;
        this.authorizedClientManager = authorizedClientManager;
    }

    @Bean
    public RestClient restClient(CustomRestClient customRestClient) {

        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

        return builder
                .requestInterceptor(customRestClient.oauth2Interceptor(authorizedClientManager))
                .requestInterceptor(interceptor)
                .build();
    }

    @Bean
    CustomRestClient getCustomRestClient() {
        return new CustomRestClientImpl();
    }
}