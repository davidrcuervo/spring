package com.laetienda.schema;

import com.laetienda.webapp_test.service.UserTestService;
import com.laetienda.webapp_test.service.UserTestServiceImplementation;
import com.laetienda.webapp_test.module.SchemaModule;
import com.laetienda.webapp_test.module.SchemaModuleImplementation;
import com.laetienda.webapp_test.service.SchemaTest;
import com.laetienda.webapp_test.service.SchemaTestImplementation;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SchemaTestConfiguration {

    @Bean
    public SchemaTest getSchemaTestService(){
        return new SchemaTestImplementation();
    }

    @Bean
    public SchemaModule getScemaTestModule(){
        return new SchemaModuleImplementation();
    }

    @Bean
    public UserTestService getUserTestService(){
        return new UserTestServiceImplementation();
    }
}
