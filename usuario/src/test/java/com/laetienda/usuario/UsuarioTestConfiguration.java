package com.laetienda.usuario;

import com.laetienda.lib.service.TestRestClient;
import com.laetienda.lib.service.TestRestClientImpl;
import com.laetienda.usuario.service.GroupTestService;
import com.laetienda.usuario.service.GroupTestServiceImplementation;
import com.laetienda.utils.service.test.UserTestService;
import com.laetienda.utils.service.test.UserTestServiceImplementation;
import com.laetienda.utils.service.api.GroupApi;
import com.laetienda.utils.service.api.GroupApiImplementation;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class UsuarioTestConfiguration {

    @Bean
	public TestRestClient getTestRestClient(){
		return new TestRestClientImpl();
	}

	@Bean
	public GroupApi getGroupApi(){
		return new GroupApiImplementation();
	}

	@Bean
	public UserTestService getUserTest(){
		return new UserTestServiceImplementation();
	}

	@Bean
	public GroupTestService getGroupTestService(){
		return new GroupTestServiceImplementation();
	}
}
