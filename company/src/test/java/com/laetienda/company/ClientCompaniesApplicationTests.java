package com.laetienda.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.model.company.Company;
import com.laetienda.utils.service.api.ApiUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ClientCompaniesApplicationTests {

	private static String jwtTestUser;

	@Autowired Environment env;
	@Autowired MockMvc mvc;
	@Autowired ObjectMapper json;
	@Autowired private ApiUser apiUser;

	@Value("${api.company.create.uri}")
	private String createAddress;

	@Value("${api.company.find.uri}")
	private String findAddress;

	@Value("${webapp.user.test.username}")
	private String testUsername;

	@Test
	void health() throws Exception {
		String address = env.getProperty("api.actuator.health.path");
		assertNotNull(address);
		mvc.perform(get(address))
				.andExpect(status().isOk());
	}

	@Test
	void cycle() throws Exception{
		Company comp = create();
		comp = findByName(comp.getName());
		comp = findById(comp.getId());
        deleteCompany(comp);
	}

	private Company create() throws Exception {
		Company company = new Company("testCycleCompany");
		company.setOwner(testUsername);

		MvcResult response = mvc.perform(post(createAddress)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsBytes(company)))
				.andExpect(status().isOk())
				.andReturn();

		return json.readValue(response.getResponse().getContentAsString(), Company.class);
	}

	private Company findById(Long id) throws Exception{
		MvcResult response = mvc.perform(get(findAddress, id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		return json.readValue(response.getResponse().getContentAsString(), Company.class);
	}

	private Company findByName(String companyName) throws Exception {
		String address = env.getProperty("api.company.findByName.uri");
		assertNotNull(address);

		MvcResult response = mvc.perform(get(address, companyName)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		return json.readValue(response.getResponse().getContentAsString(), Company.class);
	}

    private void deleteCompany(Company company) throws Exception {
        String address = env.getProperty("api.company.delete.uri");
        assertNotNull(address);
        assertNotNull(company);

        Long id = company.getId();

        MvcResult response = mvc.perform(get(findAddress, id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        mvc.perform(delete(address, id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get(findAddress, id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        mvc.perform(delete(address, id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

	@Test
	public void createWithRepeatedName(){
		fail();
	}

	@BeforeAll
	public static void setJwtTestUser(@Autowired ApiUser apiUSer, @Autowired Environment env) throws Exception{
		String testUsername = env.getProperty("webapp.user.test.username");
		assertNotNull(testUsername);

		String testUserPassword = env.getProperty("webapp.user.test.password");
		assertNotNull(testUserPassword);

		jwtTestUser = apiUSer.getToken(testUsername, testUserPassword);
	}

}
