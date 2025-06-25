package com.laetienda.webapp_test;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.webapp_test.module.SchemaModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.fail;

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
        try {
            schemaTest.cycle();
        } catch (NotValidCustomException e) {
            fail(e.getMessage());
        }
    }
}
