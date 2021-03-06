package com.laetienda.messenger;

import com.laetienda.lib.service.TestRestClient;
import com.laetienda.lib.service.TestRestClientImpl;
import com.laetienda.model.messager.EmailMessage;
import com.laetienda.model.user.Usuario;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestRestClientImpl.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessengerApplicationTests {
	final private static Logger log = LoggerFactory.getLogger(MessengerApplicationTests.class);

	final private static String ADMUSER = "admuser";
	final private static String ADMUSER_PASSWORD = "secret";
	@LocalServerPort
	private int port;

	@Autowired
	private TestRestClient restClient;

	@Value("${test.api.user.create}")
	private String urlUserCreate;

	@Value("${test.api.user.find.by.username}")
	private String urlUserFindByUsername;

	@Value("${test.api.user.delete}")
	private String urlUserDelete;

	@Value("${test.api.messenger.send.message}")
	private String urlSendMessage;

	@BeforeEach
	void createTestUser(){
		Usuario user = getTestMessengerUser();
		log.trace("urlUSerCreate: {}", urlUserCreate);
		Map<String, String> params = new HashMap<>();
		params.put("username", user.getUsername());
		ResponseEntity<Usuario> resp = restClient.send(urlUserCreate, 8081, HttpMethod.POST, user, Usuario.class, null, null, null );
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		resp = restClient.send(urlUserFindByUsername, 8081, HttpMethod.GET, null, Usuario.class, params, user.getUsername(), user.getPassword());
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		assertNotNull(resp.getBody());
	}

	@AfterEach
	void deleteTestUser(){
		Usuario user = getTestMessengerUser();
		Map<String, String> params = new HashMap<>();
		params.put("username", user.getUsername());
		ResponseEntity<Usuario> resp = restClient.send(urlUserFindByUsername, 8081, HttpMethod.GET, null, Usuario.class, params, user.getUsername(), user.getPassword());
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		assertNotNull(resp.getBody());
		ResponseEntity<String> resp1 = restClient.send(urlUserDelete, 8081, HttpMethod.DELETE, null, String.class, params, user.getUsername(), user.getPassword());
		assertEquals(HttpStatus.OK, resp1.getStatusCode());
		assertTrue(Boolean.valueOf(resp1.getBody()));
		resp = restClient.send(urlUserFindByUsername, 8081, HttpMethod.GET, null, Usuario.class, params, ADMUSER, ADMUSER_PASSWORD);
		assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
	}

	@Test
	void testSendMessage(){
		Usuario user = getTestMessengerUser();
		EmailMessage message = new EmailMessage();

		//It should fail because message is missing subject, message and addresses
		ResponseEntity<String> resp = restClient.send(urlSendMessage, port, HttpMethod.POST, message, String.class, null, user.getUsername(), user.getPassword());
		assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode(), "Message is missing subject, message and addresses");

		//It should fail because message is missing information
		message.setSubject("test");
		resp = restClient.send(urlSendMessage, port, HttpMethod.POST, message, String.class, null, user.getUsername(), user.getPassword());
		assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode(), "Message is missing arguments");

		//It should fail because message is missing information
		message.addToAddress("address@mail.com");
		resp = restClient.send(urlSendMessage, port, HttpMethod.POST, message, String.class, null, user.getUsername(), user.getPassword());
		assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode(), "Message is missing subject, message and addresses");

		//All required information has been added message should be sent

		resp = restClient.send(urlSendMessage, port, HttpMethod.POST, message, String.class, null, user.getUsername(), user.getPassword());
		assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
		assertTrue(Boolean.valueOf(resp.getBody()));
	}

	@Test
	void testSendMessageBadRequest() {
		//TODO
		//subject is null
		//email address to is null
		//email address to has not valid format
	}

	private Usuario getTestMessengerUser(){
		Usuario user = new Usuario();
		user.setUsername("junitTestMessengerUser");
		user.setFirstname("Test");
		user.setLastname("Messenger User");
		user.setEmail("junitTestMessengerUser@mail.com");
		user.setPassword("secretPassword");
		user.setPassword2("secretPassword");
		return user;
	}
}
