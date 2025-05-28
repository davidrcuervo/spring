package com.laetienda.schema;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.laetienda.webapp_test.service.UserTestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.model.schema.ItemTypeA;
import com.laetienda.webapp_test.module.SchemaModule;
//import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SchemaTestConfiguration.class)
@AutoConfigureMockMvc
class SchemaApplicationTests {
	private final static Logger log = LoggerFactory.getLogger(SchemaApplicationTests.class);

	@Autowired private Environment env;
	@Autowired private MockMvc mvc;
	@Autowired private ObjectMapper mapper;

//	@LocalServerPort
//	private int port;

//	@Value("${admuser.username}")
//	private String admuser;
//
//	@Value("${admuser.password}")
//	private String admuserPassword;

//	@BeforeEach
//	void setTest(){
//		schemaTest.setPort(port);
//	}

	@Test
	void health() throws Exception {
		String address = env.getProperty("api.actuator.health.path");
		assertNotNull(address);
		mvc.perform(get(address))
				.andExpect(status().isOk());
	}

	@Test
	void login() throws Exception {
//		schemaTest.login();
		String address = env.getProperty("api.schema.login.uri");
		assertNotNull(address);
		mvc.perform(post(address).with(jwt()))
				.andExpect(status().isOk());
	}

	@Test
	void cycle() throws Exception {
//		schemaTest.cycle();
		String clazzName = Base64.getUrlEncoder().encodeToString(ItemTypeA.class.getName().getBytes(StandardCharsets.UTF_8));
		ItemTypeA item = new ItemTypeA();
        item.setAddress("1453 Villeray");
        item.setAge(44);
        item.setUsername("myself");

		//create
		String address = env.getProperty("api.schema.create.uri");
		assertNotNull(address);
		String testUseranme = env.getProperty("webapp.user.test.username");
		assertNotNull(testUseranme);
		log.trace("TEST::cycle. $address: {}", address);
		mvc.perform(post(address, clazzName).with(jwt().jwt(jwt -> jwt.claim("preferred_username", testUseranme)))
				.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk());
	}

	@Test void createBadEditor(){
//		schemaTest.createBadEditor();
		fail();
	}

	@Test void addReader(){
//		schemaTest.addReader();
		fail();
	}

	@Test void addEditor(){
//		schemaTest.addEditor();
		fail();
	}

	@Test void removeReader(){
//		schemaTest.removeReader();
		fail();
	}

	@Test
	void simpleTest() throws Exception {
		mvc.perform(get("/api/v0/schema/simple/test"))
				.andExpect(status().isOk());
	}

	@Test void removeEditor(){fail();}
	@Test void readByBackend(){fail();}
	@Test void updateOwnerBadUnauthorized(){fail();}
	@Test void modifyOwner(){fail();}
	@Test void deleteUser(){fail();}
}