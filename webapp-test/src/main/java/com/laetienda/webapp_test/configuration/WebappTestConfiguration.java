package com.laetienda.webapp_test.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.utils.lib.UtilsBoxImplementation;
import com.laetienda.utils.service.api.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

@Configuration
public class WebappTestConfiguration {

    private final RestClient httpClient;
    private final ObjectMapper json;
    private final Environment env;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final ToolBoxService tb;

    public WebappTestConfiguration(
            RestClient restClient,
            Environment env,
            ObjectMapper json,
            OAuth2AuthorizedClientManager authorizedClientManager,
            ToolBoxService toolBoxService
    ){
        this.httpClient = restClient;
        this.json = json;
        this.env = env;
        this.authorizedClientManager = authorizedClientManager;
        this.tb = toolBoxService;
    }

    @Bean
    public ApiUser getUserApi(){
        return new ApiUserImplementation(httpClient, env, json);
    }

    @Bean
    public UtilsBox getUtilsBox(ApiUser apiUser){
        return new UtilsBoxImplementation(apiUser, authorizedClientManager);
    }

    @Bean
    public ApiSchema getApiSchema(){
        return new ApiSchemaImplementation(httpClient, json, tb);
    }

    @Bean
    public ApiSchemaGroup getApiSchemaGroup(){
        return new ApiSchemaGroupImplementation(httpClient);
    }

    @Bean
    public ApiCompany getApiCompany(){
        return new ApiCompanyImplementation(httpClient);
    }
}
