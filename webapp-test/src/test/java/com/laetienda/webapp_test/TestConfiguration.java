package com.laetienda.webapp_test;

import com.laetienda.webapp_test.module.SchemaModule;
import com.laetienda.webapp_test.module.SchemaModuleImplementation;
import org.springframework.context.annotation.Bean;

public class TestConfiguration {
    @Bean
    public SchemaModule getSchemaTestModule(){
        return new SchemaModuleImplementation();
    }
}
