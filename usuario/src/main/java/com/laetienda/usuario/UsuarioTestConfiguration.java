package com.laetienda.usuario;

import com.laetienda.lib.service.TestRestClient;
import com.laetienda.lib.service.TestRestClientImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class UsuarioTestConfiguration {

    @Bean
	public TestRestClient getTestRestClient(){
		return new TestRestClientImpl();
	}
}
