package com.laetienda.schema;

import com.laetienda.utils.service.test.SchemaTest;
import com.laetienda.utils.service.test.SchemaTestImplementation;
import com.laetienda.utils.service.test.UserTestService;
import com.laetienda.utils.service.test.UserTestServiceImplementation;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SchemaTestConfiguration {

    @Bean
    public SchemaTest getSchemaTestService(){
        return new SchemaTestImplementation();
    }

    @Bean
    public UserTestService getUserTestService(){
        return new UserTestServiceImplementation();
    }
}
