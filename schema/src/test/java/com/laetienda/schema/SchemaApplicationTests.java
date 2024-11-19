package com.laetienda.schema;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.laetienda.webapp_test.service.UserTestService;
import com.laetienda.webapp_test.module.SchemaModule;
//import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
//import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SchemaTestConfiguration.class)
class SchemaApplicationTests {
	private final static Logger log = LoggerFactory.getLogger(SchemaApplicationTests.class);

	@Autowired private SchemaModule schemaTest;

	@LocalServerPort
	private int port;

	@Value("${admuser.username}")
	private String admuser;

	@Value("${admuser.password}")
	private String admuserPassword;

	@BeforeEach
	void setTest(){
		schemaTest.setPort(port);
	}

	@Test void login(){schemaTest.login();}
	@Test void cycle(){schemaTest.cycle();}
	@Test void createBadEditor(){schemaTest.createBadEditor();}
	@Test void addReader(){schemaTest.addReader();}
	@Test void addEditor(){fail();}
	@Test void removeReader(){fail();}
	@Test void removeEditor(){fail();}
	@Test void readByBackend(){fail();}
	@Test void updateOwnerBadUnauthorized(){fail();}
	@Test void deleteUser(){fail();}
}