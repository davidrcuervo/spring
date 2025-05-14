package com.laetienda.kcUser.configuration;

import com.laetienda.lib.service.CustomRestClient;
import com.laetienda.lib.service.CustomRestClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    public RestClient getRestClient(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager
    ){
        return builder.requestInterceptor(getCustomRestClient().oauth2Interceptor(authorizedClientManager)).build();
    }

    @Bean
    CustomRestClient getCustomRestClient(){
        return new CustomRestClientImpl();
    }
}
