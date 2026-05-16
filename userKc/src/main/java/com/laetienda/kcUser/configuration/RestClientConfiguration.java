package com.laetienda.kcUser.configuration;

import com.laetienda.lib.service.CustomRestClient;
import com.laetienda.lib.service.CustomRestClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    private final RestClient.Builder builder;
    private final OAuth2AuthorizedClientManager oauth2AuthorizedClientManager;

    public RestClientConfiguration(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager oauth2AuthorizedClientManager
    ){
        this.builder = builder;
        this.oauth2AuthorizedClientManager = oauth2AuthorizedClientManager;
    }

    @Bean
    public RestClient getRestClient(CustomRestClient customRestClient){
        OAuth2ClientHttpRequestInterceptor requestInterceptor =
                new OAuth2ClientHttpRequestInterceptor(oauth2AuthorizedClientManager);

        return builder
                .requestInterceptor(customRestClient.oauth2Interceptor(oauth2AuthorizedClientManager))
                .requestInterceptor(requestInterceptor)
                .build();
    }

    @Bean
    CustomRestClient getCustomRestClient(){
        return new CustomRestClientImpl();
    }
}
