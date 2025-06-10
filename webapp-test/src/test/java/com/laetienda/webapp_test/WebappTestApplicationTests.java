package com.laetienda.webapp_test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebappTestApplicationTests {

	@Value("${admuser.username}")
	private String admuser;

	@Value("${admuser.password}")
	private String admuserPassword;

	@Test
	void contextLoads() {
		assertEquals("admuser", admuser);
		assertEquals("secret", admuserPassword);
	}

}
