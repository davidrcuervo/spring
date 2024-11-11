package com.laetienda.schema;

import com.laetienda.model.schema.DbItem;
import com.laetienda.utils.service.test.SchemaTest;
import com.laetienda.utils.service.test.UserTestService;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SchemaTestConfiguration.class)
class SchemaApplicationTests {
	private final static Logger log = LoggerFactory.getLogger(SchemaApplicationTests.class);

	@Autowired private SchemaTest schemaTest;
	@Autowired private UserTestService userTest;
	@Autowired private Environment env;
	@Autowired private StringEncryptor jasypte;

	@LocalServerPort
	private int port;

	@Value("${admuser.username}")
	private String admuser;

	private String admuserPassword;

	@BeforeEach
	void setApiPort(){
		admuserPassword = jasypte.decrypt(env.getProperty("admuser.hashed.password"));
		schemaTest.setPort(port);
		schemaTest.setAdmuserPassword(admuserPassword);
		userTest.setAdmuserPassword(admuserPassword);
	}

	@Test
	void helloAll() {
		schemaTest.helloAll();
	}

	@Test
	void helloUser(){
		schemaTest.helloUser(admuser, admuserPassword);
	}

	@Test
	void helloValidateUser(){
		schemaTest.helloValidateUser(admuser, admuserPassword);
	}

	@Test
	void create() {
		DbItem item = new DbItem();
		schemaTest.create(item);
	}

	@Test
	void createBadEditor(){
		DbItem item = new DbItem();
		item.addEditor("NonExistentEditor");
		schemaTest.createBadEditor(item);
	}
}