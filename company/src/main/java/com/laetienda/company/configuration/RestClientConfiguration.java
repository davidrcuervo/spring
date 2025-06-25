package com.laetienda.company.configuration;

import com.laetienda.lib.service.CustomRestClient;
import com.laetienda.lib.service.CustomRestClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    public RestClient getRestClient(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager
    ){
        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

        return builder
                .requestInterceptor(getCustomRestClient().oauth2Interceptor(authorizedClientManager))
                .requestInterceptor(interceptor)
                .build();
    }

    @Bean
    CustomRestClient getCustomRestClient(){
        return new CustomRestClientImpl();
    }
}
