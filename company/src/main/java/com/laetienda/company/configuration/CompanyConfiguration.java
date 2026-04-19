package com.laetienda.company.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.utils.lib.UtilsBoxImplementation;
import com.laetienda.utils.service.api.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

@Configuration
public class CompanyConfiguration {

    private final RestClient client;
    private final ObjectMapper mapper;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public CompanyConfiguration(
            RestClient restClient,
            ObjectMapper mapper,
            OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager
    ){
        this.client = restClient;
        this.mapper = mapper;
        this.authorizedClientManager = oAuth2AuthorizedClientManager;
    }

    @Bean
    public ApiSchema getApiSchema(){
        return new ApiSchemaImplementation(client);
    }

    @Bean
    public ApiUser getApiUser(){
        return new ApiUserImplementation(client);
    }

    @Bean
    public ApiSchemaGroup getApiSchemaGroup(){
        return new ApiSchemaGroupImplementation(client);
    }

    @Bean
    public ApiMessenger getApiMessenger() {
        return new ApiMessengerImplementation(client);
    }

    @Bean
    public UtilsBox getUtilsBox(ApiUser apiUser){
        return new UtilsBoxImplementation(apiUser, authorizedClientManager);
    }
}
