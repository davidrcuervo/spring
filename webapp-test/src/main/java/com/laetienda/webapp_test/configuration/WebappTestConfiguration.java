package com.laetienda.webapp_test.configuration;

import com.laetienda.utils.service.api.SchemaApi;
import com.laetienda.utils.service.api.SchemaApiImplementation;
import com.laetienda.utils.service.api.UserApi;
import com.laetienda.utils.service.api.UserApiImplementation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebappTestConfiguration {

    @Bean
    public UserApi getUserApi(){
        return new UserApiImplementation();
    }

    @Bean
    public SchemaApi getSchemaApi(){
        return new SchemaApiImplementation();
    }
}
