package com.laetienda.messenger;

import com.laetienda.model.messager.EmailMessage;
import com.laetienda.model.user.Usuario;
import com.laetienda.utils.service.api.MessengerApi;
import com.laetienda.utils.service.api.UserApiDeprecated;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessengerApplicationTests {
	final private static Logger log = LoggerFactory.getLogger(MessengerApplicationTests.class);

	@Value("${local.server.port}")
	private int port;

	@Value("${api.usuario.port}")
	private String apiUserPort;

	@Autowired
	private MessengerApi messengerApi;

	@Autowired
	private UserApiDeprecated userApiDeprecated;

	@BeforeEach
	public void setPorts(){
		userApiDeprecated.setPort(apiUserPort);
	}

	@Test
	void testSendMessage(){
		log.trace("MESSENGER_TEST::testSendMessage");

		EmailMessage message = new EmailMessage();

		//It should fail because message is missing subject, message and addresses
		HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
			ResponseEntity<String> resp =
					((MessengerApi)messengerApi.setPort(port))
					.sendMessage(message);
		});
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode(), "Message is missing subject, message and addresses");

		//It should fail because message is missing information
		message.setSubject("Web project. Spring Java Mailer test");
		HttpClientErrorException ex1 = assertThrows(HttpClientErrorException.class, () -> {
			ResponseEntity<String> resp = messengerApi.sendMessage(message);
		});
		assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatusCode());

		//It should fail because message is missing information
		message.addToAddress("davidrcuervo@outlook.com");
		HttpClientErrorException ex2 = assertThrows(HttpClientErrorException.class, () -> {
			ResponseEntity<String> resp = messengerApi.sendMessage(message);
		});
		assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode(), "Message is missing subject, message and addresses");

		//All required information has been added message should be sent
		message.setView("default/test.html");
		ResponseEntity<String> resp = messengerApi.sendMessage(message);
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		assertNotNull(resp.getBody());
		assertTrue(Boolean.valueOf(resp.getBody()));
	}

	@Test
	public void testHelloWorld(){
		ResponseEntity<String> resp = ((MessengerApi)messengerApi.setPort(port)).helloWord();
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		assertNotNull(resp.getBody());
		assertEquals("Hola mundo", resp.getBody());
	}

	@Test
	public void testSimplePost(){
		Usuario user = new Usuario(
				"testSimplePost",
				"Simple","Post","Test Messenger",
				"testSimplePost@mail.com",
				"secretpassword","secretpassword"
		);

		HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
			((MessengerApi)messengerApi.setPort(port)).testSimplePost(user);
		});

		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
	}
}
