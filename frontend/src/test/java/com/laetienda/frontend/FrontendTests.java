package com.laetienda.frontend;

import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.lib.UtilsBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
@Import(FrontendTestConfiguration.class)
class FrontendTests {

	private static TestUserDto[] USERS;

	@Autowired private Environment env;
	@Autowired private MockMvc mvc;
	@Autowired private UtilsBox utilsBox;

	@Test
	void health() throws Exception {
		String address = env.getProperty("api.actuator.health.path", "#");
		mvc.perform(get(address))
				.andExpect(status().isOk());
	}

//	@Test
	void shutdown() throws Exception {
		String address = String.format("%s/shutdown", env.getProperty("api.actuator.folder", "actuator"));
		mvc.perform(post(address))
				.andExpect(status().isOk());
	}

	@Test
	public void notFoundTemplate() throws Exception {
		mvc.perform(get("/wrongAddress")
						.accept(MediaType.TEXT_HTML_VALUE)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
				.andExpect(status().isNotFound());
	}

	@Test
	public void simpleTest() throws Exception {
	    fail();
	}

	@BeforeAll
	static void setup(@Autowired UtilsBox utils) {
		USERS = utils.getTestUsers(2, "FRONTEND");
	}

	@AfterAll
	static void tearDown(@Autowired UtilsBox utilsBox) {
		utilsBox.deleteTestUsers(USERS);
	}

}
