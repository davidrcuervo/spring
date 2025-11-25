package com.laetienda.company.configuration;

import com.laetienda.utils.service.api.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CompanyConfiguration {

    private final RestClient client;

    public CompanyConfiguration(RestClient restClient){
        this.client = restClient;
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
    public ApiMessenger getApiMessenger() {
        return new ApiMessengerImplementation(client);
    }
}
