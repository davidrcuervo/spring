package com.laetienda.schema.configuration;

import com.laetienda.utils.service.api.SchemaApi;
import com.laetienda.utils.service.api.SchemaApiImplementation;
import com.laetienda.utils.service.api.UserApi;
import com.laetienda.utils.service.api.UserApiImplementation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchemaConfiguration {

    @Bean
    public UserApi getUserApiService(){
        return new UserApiImplementation();
    }

    @Bean
    public SchemaApi getSchemaApiService(){
        return new SchemaApiImplementation();
    }
}