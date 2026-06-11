package com.laetienda.kcUser.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.lib.service.ToolBoxServiceImpl;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.utils.lib.UtilsBoxImplementation;
import com.laetienda.utils.service.api.ApiUser;
import com.laetienda.utils.service.api.ApiUserImplementation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

@Configuration
public class KcUserConfiguration {

    private final OAuth2AuthorizedClientManager oauth2AuthorizedClientManager;
    private final RestClient client;
    private final Environment env;
    private final ObjectMapper json;

    KcUserConfiguration(
            OAuth2AuthorizedClientManager oauth2AuthorizedClientManager,
            RestClient restClient,
            Environment environment,
            ObjectMapper objectMapper
    ) {
        this.oauth2AuthorizedClientManager = oauth2AuthorizedClientManager;
        this.client = restClient;
        this.env = environment;
        this.json = objectMapper;
    }

    @Bean
    public ToolBoxService getToolBox(){
        return new ToolBoxServiceImpl();
    }

    @Bean
    public ApiUser getApiUser(ToolBoxService tb){
        return new ApiUserImplementation(client, env, json, tb);
    }

    @Bean
    public UtilsBox getUtilsBox(ApiUser apiUser){
        return new UtilsBoxImplementation(apiUser, oauth2AuthorizedClientManager);
    }
}
