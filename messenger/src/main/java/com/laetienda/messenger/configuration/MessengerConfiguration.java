package com.laetienda.messenger.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.lib.service.ToolBoxServiceImpl;
import com.laetienda.utils.service.api.ApiSchema;
import com.laetienda.utils.service.api.ApiSchemaImplementation;
import com.laetienda.utils.service.api.ApiUser;
import com.laetienda.utils.service.api.ApiUserImplementation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;

@Configuration
public class MessengerConfiguration {

    private final RestClient client;
    private final Environment env;
    private final ObjectMapper json;

    public MessengerConfiguration(
            RestClient restClient,
            Environment environment,
            ObjectMapper objectMapper
    ){
        this.client = restClient;
        this.env = environment;
        this.json = objectMapper;
    }

    @Bean
    public ToolBoxService getToolBoxService(){
        return new ToolBoxServiceImpl();
    }

    @Bean
    public ApiSchema getApiSchema(ToolBoxService tb){
        return new ApiSchemaImplementation(client, json, tb);
    }

    @Bean
    public ApiUser getApiUser(){
        return new ApiUserImplementation(client, env, json);
    }

//    @Bean
    public SpringTemplateEngine getSpringTemplateEngine(){
        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(getEmailTemplateResolver());
        return springTemplateEngine;
    }

    private ClassLoaderTemplateResolver getEmailTemplateResolver() {
        ClassLoaderTemplateResolver emailTemplateResolver = new ClassLoaderTemplateResolver();
        emailTemplateResolver.setPrefix("/templates/");
        emailTemplateResolver.setSuffix(".html");
        emailTemplateResolver.setTemplateMode(TemplateMode.HTML);
        emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        emailTemplateResolver.setCacheable(false);
        return emailTemplateResolver;
    }



}
