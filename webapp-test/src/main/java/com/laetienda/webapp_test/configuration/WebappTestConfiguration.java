package com.laetienda.webapp_test.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.utils.lib.UtilsBoxImplementation;
import com.laetienda.utils.service.api.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

@Configuration
public class WebappTestConfiguration {

    private final RestClient httpClient;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final ObjectMapper objectMapper;

    public WebappTestConfiguration(
            RestClient restClient,
            OAuth2AuthorizedClientManager authorizedClientManager,
            ObjectMapper objectMapper
    ){
        this.httpClient = restClient;
        this.authorizedClientManager = authorizedClientManager;
        this.objectMapper = objectMapper;
    }

    @Bean
    public ApiUser getUserApi(){
        return new ApiUserImplementation(httpClient);
    }

    @Bean
    public UtilsBox getUtilsBox(ApiUser apiUser){
        return new UtilsBoxImplementation(apiUser, authorizedClientManager);
    }

    @Bean
    public ApiSchema getApiSchema(){
        return new ApiSchemaImplementation(httpClient);
    }

    @Bean
    public ApiSchemaGroup getApiSchemaGroup(){
        return new ApiSchemaGroupImplementation(httpClient,  objectMapper);
    }

}
