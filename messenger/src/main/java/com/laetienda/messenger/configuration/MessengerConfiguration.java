package com.laetienda.messenger.configuration;

import com.laetienda.utils.service.api.ApiSchema;
import com.laetienda.utils.service.api.ApiSchemaImplementation;
import com.laetienda.utils.service.api.ApiUser;
import com.laetienda.utils.service.api.ApiUserImplementation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.thymeleaf.spring5.SpringTemplateEngine;
import org.springframework.web.client.RestClient;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.nio.charset.StandardCharsets;

@Configuration
public class MessengerConfiguration {

    private final RestClient client;

    public MessengerConfiguration(RestClient restClient){
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
