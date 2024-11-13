package com.laetienda.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.model.schema.ItemTypeA;
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
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SchemaTestConfiguration.class)
class SchemaApplicationTests {
	private final static Logger log = LoggerFactory.getLogger(SchemaApplicationTests.class);

	@Autowired private SchemaTest schemaTest;
	@Autowired private UserTestService userTest;
	@Autowired private Environment env;
	@Autowired private StringEncryptor jasypte;
	@Autowired private ObjectMapper jsonMapper;

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
	void cycle(){
		ItemTypeA item = new ItemTypeA();
		item.setAddress("1453 Villeary");
		item.setAge(43);
		item.setUsername("myself");

		item = create(item);
		find(item);
		delete(item);
	}

	ItemTypeA create(ItemTypeA item){

		ResponseEntity<ItemTypeA> resp = schemaTest.create(ItemTypeA.class, item);
		ItemTypeA itemResp = resp.getBody();
		assertTrue(itemResp.getId() > 0);
		assertEquals(item.getAge(), itemResp.getAge());
		assertEquals(item.getUsername(), itemResp.getUsername());
		assertEquals(item.getAddress(), itemResp.getAddress());

		return itemResp;
	}

	void find(ItemTypeA item) {
		Map<String, String> body = new HashMap<String, String>();
		body.put("username", item.getUsername());
		ResponseEntity<ItemTypeA> resp = schemaTest.find(ItemTypeA.class, body);
		assertEquals(item.getId(), resp.getBody().getId());
	}

	void delete(ItemTypeA item){
		Map<String, String> body = new HashMap<String, String>();
		body.put("username", item.getUsername());
		schemaTest.delete(ItemTypeA.class, body);
		schemaTest.notFound(ItemTypeA.class, body);
	}

	@Test
	void createBadEditor(){
		ItemTypeA item = new ItemTypeA();
		item.addEditor("NonExistentEditor");
		schemaTest.createBadEditor(ItemTypeA.class, item);
	}

	@Test
	void update(){
		fail();
	}

	@Test
	void addReader(){
		fail();
	}

	@Test
	void addEditor(){
		fail();
	}

	@Test
	void removeReader(){
		fail();
	}

	@Test
	void removeEditor(){
		fail();
	}

	@Test
	void deleteUser(){
		fail();
	}
}