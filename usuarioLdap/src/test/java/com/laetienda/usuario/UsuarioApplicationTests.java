package com.laetienda.usuario;

import com.laetienda.webapp_test.module.UserModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@Import({UsuarioTestConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsuarioApplicationTests {

	@Autowired
	private UserModule userTest;

	@LocalServerPort private int port;

	@BeforeEach
	void setPort(){
		userTest.setPort(port);
	}

	@Test
	void testAuthentication(){
		userTest.testAuthentication();
	}

	@Test
	void testAuthenticationWithIvalidUsername(){
		userTest.testAuthenticationWithIvalidUsername();
	}

	@Test
	void testFindAll(){
		userTest.testFindAll();
	}

	@Test
	void testFindAllUnautorized(){
		userTest.testFindAllUnautorized();
	}

	@Test
	void testFindByUsername(){
		userTest.testFindByUsername();
	}

	@Test
	void testFindByUsernameRoleManager(){
		userTest.testFindByUsernameRoleManager();
	}

	@Test
	void testFindByUsernameUnauthorized(){
		userTest.testFindByUsernameUnauthorized();
	}

	@Test
	void testFindByUsernameNotFound(){
		userTest.testFindByUsernameNotFound();
	}

	@Test
	void testUserCycle(){
		userTest.testUserCycle();
	}

	@Test
	void testCreateUserRepeatedUsername(){
		userTest.testCreateUserRepeatedUsername();
	}

	@Test
	void testCreateUserRepeatedEmail(){
		userTest.testCreateUserRepeatedEmail();
	}

	@Test
	void testCreateUserBadPassword(){
		userTest.testCreateUserBadPassword();
	}

	@Test
	void testDeleteNotFound(){
		userTest.testDeleteNotFound();
	}

	@Test
	void testDeleteUnauthorized(){
		userTest.testDeleteUnauthorized();
	}

	@Test
	void testDeleteAdmuser(){
		userTest.testDeleteAdmuser();
	}

	@Test
	void testFindApplicationProfiles(){
		userTest.testFindApplicationProfiles();
	}

	@Test
	void testApi(){
		userTest.testApi();
	}

	@Test
	void deleteApiUser(){
		userTest.deleteApiUser();
	}

	@Test
	void testCreateWithAuthenticatedUser(){
		userTest.testCreateWithAuthenticatedUser();
	}

	@Test
	void login(){
		userTest.login();
	}

	@Test
	void session(){
		userTest.session();
	}
}
