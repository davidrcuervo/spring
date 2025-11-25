package com.laetienda.messenger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.model.messager.EmailMessage;
import com.laetienda.utils.service.api.ApiUser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MessengerApplicationTests {
	final private static Logger log = LoggerFactory.getLogger(MessengerApplicationTests.class);

    private static String jwtTestUser;

    @Autowired private Environment env;
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private ApiUser apiUser;

    @Value("${webapp.user.test.userId}")
    private String testUserId;

    @Test
    void health() throws Exception{
        String address = env.getProperty("api.actuator.health.path");
        assertNotNull(address);

        mvc.perform(get(address))
                .andExpect(status().isOk());
    }

    @Test
    void test() throws Exception {
        String address = env.getProperty("api.mail.uri.test");
        assertNotNull(address);

        mvc.perform(get(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTestUser()))
                .andExpect(status().isNoContent());
    }

	@Test
	void send() throws Exception {
        String address = env.getProperty("api.mail.uri.send");
        assertNotNull(address);

		EmailMessage message = new EmailMessage();

		//It should fail because message is missing subject, message and addresses
		mvc.perform(post(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTestUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(message)))
                .andExpect(status().isBadRequest());

		//It should fail because message is missing information
		message.setSubject("Web project. Spring Java Mailer test");
		mvc.perform(post(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTestUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(message)))
                .andExpect(status().isBadRequest());

		//It should fail because message is missing information
		message.addToAddress("davidrcuervo@outlook.com");
		mvc.perform(post(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTestUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(message)))
                .andExpect(status().isBadRequest());

		//All required information has been added message should be sent
		message.setView("default/test.html");
		mvc.perform(post(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTestUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(message)))
                .andExpect(status().isNoContent());
	}

    public String getJwtTestUser() throws Exception {

        if(jwtTestUser == null){
            String username = env.getProperty("webapp.user.test.username");
            String password = env.getProperty("webapp.user.test.password");
            jwtTestUser = apiUser.getToken(username, password);
        }

        return jwtTestUser;
    }
}
