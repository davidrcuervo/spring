package com.laetienda.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.model.schema.ItemTypeA;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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

	@Value("${webapp.user.test.username}")
	private String testUsername;

	@Value("${webapp.user.test.userId}")
	private String testUserId;

//	@LocalServerPort
//	private int port;

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

		create(item, clazzName);
		item = find(item, clazzName);
		assertEquals("1453 Villeray", item.getAddress());
		item = update(item, clazzName);
		assertNotEquals("1453 Villeray", item.getAddress());
		delete(item, clazzName);

	}

	private void create(ItemTypeA item, String clazzName) throws Exception{
		String address = env.getProperty("api.schema.create.uri");
		assertNotNull(address);

		mvc.perform(post(address, clazzName)
						.with(jwt()
								.jwt(jwt -> jwt
										.claim("preferred_username", testUsername)
										.claim("sub", testUserId)))
						.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk());
	}

	private ItemTypeA find(ItemTypeA item, String clazzName) throws Exception {
		String address = env.getProperty("api.schema.find.uri");
		assertNotNull(address);

		Map<String, String> body = new HashMap<String, String>();
		body.put("username", item.getUsername());

		MvcResult result = mvc.perform(post(address, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
				.content(mapper.writeValueAsBytes(body))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.address").value(item.getAddress()))
				.andReturn();

		return mapper.readValue(result.getResponse().getContentAsString(), ItemTypeA.class);
	}

	private ItemTypeA update(ItemTypeA item, String clazzName) throws Exception {
		String address = env.getProperty("api.schema.update.uri");
		assertNotNull(address);

		item.setAddress("5 Place Ville Marie");
		MvcResult response = mvc.perform(put(address, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.address").value("5 Place Ville Marie"))
				.andReturn();

		return mapper.readValue(response.getResponse().getContentAsString(), ItemTypeA.class);
	}

	private void delete(ItemTypeA item, String clazzName) throws Exception {
		String address = env.getProperty("api.schema.delete.uri");
		assertNotNull(address);

		Map<String, String> body = new HashMap<String, String>();
		body.put("username", item.getUsername());

		MvcResult response = mvc.perform(post(address, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(body)))
				.andExpect(status().isOk())
				.andReturn();

		assertTrue(Boolean.parseBoolean(response.getResponse().getContentAsString()));

		String findAddress = env.getProperty("api.schema.find.uri");
		assertNotNull(address);

		mvc.perform(post(address, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(body)))
				.andExpect(status().isNotFound());
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