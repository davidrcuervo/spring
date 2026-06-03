package com.laetienda.schema.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.lib.service.ToolBoxServiceImpl;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.utils.lib.UtilsBoxImplementation;
import com.laetienda.utils.service.api.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

@Configuration
@EntityScan(basePackages={
        "com.laetienda.model.schema"
        ,"com.laetienda.model.company"
})
public class SchemaConfiguration {

    private final RestClient client;
    private final ObjectMapper json;
    private final Environment env;

    SchemaConfiguration(
            RestClient restClient,
            ObjectMapper objectMapper,
            Environment environment
    ){
        this.client = restClient;
        this.json = objectMapper;
        this.env = environment;
    }

    @Bean
    public ApiUser getUserApi(ToolBoxService tb){
        return new ApiUserImplementation(client, env,  json, tb);
    }

    @Bean
    public ToolBoxService getToolBox(){
        return new ToolBoxServiceImpl();
    }

    @Bean
    public UtilsBox getUtilsBox(ApiUser apiUser, OAuth2AuthorizedClientManager authorizedClientManager) {
        return new UtilsBoxImplementation(apiUser, authorizedClientManager);
    }
}
