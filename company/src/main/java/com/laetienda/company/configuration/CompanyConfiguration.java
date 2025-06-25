package com.laetienda.company.configuration;

import com.laetienda.utils.service.api.ApiSchema;
import com.laetienda.utils.service.api.ApiSchemaImplementation;
import com.laetienda.utils.service.api.ApiUser;
import com.laetienda.utils.service.api.ApiUserImplementation;
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
}
