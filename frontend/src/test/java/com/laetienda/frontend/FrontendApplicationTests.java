package com.laetienda.frontend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
class FrontendApplicationTests {

	@Autowired private Environment env;
	@Autowired private MockMvc mvc;

	@Test
	void health() throws Exception {
		String address = env.getProperty("api.actuator.health.path", "#");
		mvc.perform(get(address))
				.andExpect(status().isOk());
	}

}
