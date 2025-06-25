package com.laetienda.schema.configuration;

import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.lib.service.ToolBoxServiceImpl;
import com.laetienda.utils.service.api.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EntityScan(basePackages={
        "com.laetienda.model.schema"
        ,"com.laetienda.model.company"
})
public class SchemaConfiguration {

    private final RestClient client;

    SchemaConfiguration(RestClient restClient){
        this.client = restClient;
    }

    @Bean
    public ApiUser getUserApi(){
        return new ApiUserImplementation(client);
    }

    @Bean
    public ToolBoxService getToolBox(){
        return new ToolBoxServiceImpl();
    }

//    @Bean
//    public UserApiDeprecated getUserApiService(){
//        return new UserApiDeprecatedImplementation();
//    }
//
//    @Bean
//    public ApiSchema getSchemaApiService(){
//        return new ApiSchemaImplementationDeprecated();
//    }
}
