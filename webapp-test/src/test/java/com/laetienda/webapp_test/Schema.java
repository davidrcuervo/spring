package com.laetienda.webapp_test;

import com.laetienda.webapp_test.module.SchemaModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfiguration.class)
public class Schema {
    @Autowired
    SchemaModule schemaTest;

    @Value("${api.schema.port}")
    private String port;

    @BeforeEach
    void setSchemaTest(){
        schemaTest.setPort(Integer.parseInt(port));
    }

    @Test
    void cycle(){
        schemaTest.cycle();
    }
}
