package com.laetienda.webapp_test.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.lib.service.ToolBoxServiceImpl;
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

    public WebappTestConfiguration(
            RestClient restClient,
            Environment env,
            ObjectMapper json,
            OAuth2AuthorizedClientManager authorizedClientManager
    ){
        this.httpClient = restClient;
        this.json = json;
        this.env = env;
        this.authorizedClientManager = authorizedClientManager;
    }

    @Bean
    public ApiUser getUserApi(ToolBoxService tb){
        return new ApiUserImplementation(httpClient, env, json, tb);
    }

    @Bean
    public ToolBoxService getToolBoxService(){
        return new ToolBoxServiceImpl();
    }

    @Bean
    public UtilsBox getUtilsBox(ApiUser apiUser){
        return new UtilsBoxImplementation(apiUser, authorizedClientManager);
    }

    @Bean
    public ApiSchema getApiSchema(ToolBoxService tb){
        return new ApiSchemaImplementation(httpClient, json, tb);
    }

    @Bean
    public ApiSchemaGroup getApiSchemaGroup(ToolBoxService tb){
        return new ApiSchemaGroupImplementation(httpClient, tb);
    }

    @Bean
    public ApiCompany getApiCompany(ToolBoxService tb){
        return new ApiCompanyImplementation(httpClient, tb);
    }
}
