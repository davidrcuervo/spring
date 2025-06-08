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
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
	private final String clazzName = Base64.getUrlEncoder().encodeToString(ItemTypeA.class.getName().getBytes(StandardCharsets.UTF_8));

	@Autowired private Environment env;
	@Autowired private MockMvc mvc;
	@Autowired private ObjectMapper mapper;

	@Value("${webapp.user.test.username}")
	private String testUsername;

	@Value("${webapp.user.test.userId}")
	private String testUserId;

	@Value("${webapp.user.admin.userId}")
	private String adminUserId;

    @Value("${webapp.user.service.userId}")
    private String serviceUserId;

	@Value("${api.schema.update.uri}")
	private String updateAddress;

	@Value("${api.schema.deleteById.uri}")
	private String deleteAddress;

    @Value("${api.schema.create.uri}")
    private String createAddress;

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
//		String clazzName = Base64.getUrlEncoder().encodeToString(ItemTypeA.class.getName().getBytes(StandardCharsets.UTF_8));
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

		mvc.perform(post(createAddress, clazzName)
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

	@Test void setEditor() throws Exception{
//		schemaTest.createBadEditor();
//		schemaTest.addEditor();

		String address = env.getProperty("api.schema.create.uri");
		assertNotNull(address);

		ItemTypeA item = new ItemTypeA("createBadEditor", 22, "7775 Des Erables");
		item.addReader(testUserId);

		//create item
		MvcResult response = mvc.perform(post(address, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk())
				.andReturn();
		item = mapper.readValue(response.getResponse().getContentAsString(), ItemTypeA.class);

		//try to edit by not valid editor
		item.setAge(44);
		mvc.perform(put(updateAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isUnauthorized());

		//add editor
		item.addEditor(testUserId);
		item.setAge(22);
		MvcResult response2 = mvc.perform(put(updateAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk())
				.andReturn();
		item = mapper.readValue(response2.getResponse().getContentAsString(), ItemTypeA.class);

		//try to edit by valid editor
		item.setAge(42);
		mvc.perform(put(updateAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.age").value(42));

		//remove editor
		item.removeEditor(testUserId);
		mvc.perform(put(updateAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk());

		//delete by using removed editor
		mvc.perform(MockMvcRequestBuilders.delete(deleteAddress, item.getId(), clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
				.andExpect(status().isUnauthorized());

		//delete by using owner editor
		mvc.perform(MockMvcRequestBuilders.delete(deleteAddress, item.getId(), clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId))))
				.andExpect(status().isOk());
	}

	@Test void setReader() throws Exception{
//		schemaTest.addReader();
		ItemTypeA item = new ItemTypeA("schemaAddReader", 22, "Calle 70B # 87B - 24");
		Map<String, String> body = new HashMap<>();
		body.put("username", item.getUsername());

		//Create item
		String createAddress = env.getProperty("api.schema.create.uri");
		assertNotNull(createAddress);
		MvcResult response = mvc.perform(post(createAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk())
				.andReturn();
		item = mapper.readValue(response.getResponse().getContentAsString(), ItemTypeA.class);

		//Read item as test user. it should fail
		String findAddress = env.getProperty("api.schema.find.uri");
		assertNotNull(findAddress);
		mvc.perform(post(findAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(body)))
				.andExpect(status().isUnauthorized());

		//Add reader and try again
		item.addReader(testUserId);
		String updateAddress = env.getProperty("api.schema.update.uri");
		assertNotNull(updateAddress);
		mvc.perform(put(updateAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk());

		//Read item as test user again. It should work now
		mvc.perform(post(findAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(body)))
				.andExpect(status().isOk());

		//Remove reader
		//schemaTest.removeReader();
		item.removeReader(testUserId);
		mvc.perform(put(updateAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk());

		mvc.perform(post(findAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(body)))
				.andExpect(status().isUnauthorized());

		//delete item from database to clean up
		String deleteAddress = env.getProperty("api.schema.delete.uri");
		assertNotNull(deleteAddress);
		mvc.perform(post(deleteAddress, clazzName)
				.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(body)))
				.andExpect(status().isOk());
	}

	@Test void readByBackend() throws Exception {
        ItemTypeA item = new ItemTypeA("readByBackend", 37, "1140 St. Catherine");
        mvc.perform(post(createAddress, clazzName)
                .with(jwt().jwt(jwt -> jwt.claim("sub", serviceUserId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(item)))
                .andExpect(status().isUnauthorized());

        //create test item by using test user
        MvcResult response = mvc.perform(post(createAddress, clazzName)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(item)))
                .andExpect(status().isOk())
                .andReturn();
        ItemTypeA itemResponse = mapper.readValue(response.getResponse().getContentAsString(), ItemTypeA.class);

        //try to add service user as reader
        itemResponse.addEditor(serviceUserId);
        mvc.perform(MockMvcRequestBuilders.put(updateAddress, clazzName)
                .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(itemResponse)))
                .andExpect(status().isBadRequest());

        //remove test item
        mvc.perform(MockMvcRequestBuilders.delete(deleteAddress, itemResponse.getId(), clazzName)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
                .andExpect(status().isOk());
    }

	@Test void modifyOwner() throws Exception {
		ItemTypeA item = new ItemTypeA("modifyOwner", 19, "17 Villaluz");

		//create a test item
		MvcResult response = mvc.perform(post(createAddress, clazzName)
						.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId)))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsBytes(item)))
				.andExpect(status().isOk())
				.andReturn();
		ItemTypeA itemResponse = mapper.readValue(response.getResponse().getContentAsString(), ItemTypeA.class);

		//test by setting wrong owner
		itemResponse.setOwner(serviceUserId);
		mvc.perform(put(updateAddress, clazzName)
						.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId)))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsBytes(itemResponse)))
				.andExpect(status().isBadRequest());

		//test by updating owner by not current owner
		itemResponse.setOwner(testUserId);
		mvc.perform(put(updateAddress, clazzName)
						.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsBytes(itemResponse)))
				.andExpect(status().isUnauthorized());

		//update owner successfully
		mvc.perform(put(updateAddress, clazzName)
						.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId)))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsBytes(itemResponse)))
				.andExpect(status().isOk());

		//remove test item
		mvc.perform(MockMvcRequestBuilders.delete(deleteAddress, itemResponse.getId(), clazzName)
						.with(jwt().jwt(jwt -> jwt.claim("sub", adminUserId))))
				.andExpect(status().isUnauthorized());

		mvc.perform(MockMvcRequestBuilders.delete(deleteAddress, itemResponse.getId(), clazzName)
						.with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
				.andExpect(status().isOk());
	}

	@Test void deleteUser(){fail();}
}