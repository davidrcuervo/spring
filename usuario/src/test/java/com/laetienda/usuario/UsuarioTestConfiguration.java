package com.laetienda.usuario;

import com.laetienda.lib.service.TestRestClient;
import com.laetienda.lib.service.TestRestClientImpl;
import com.laetienda.webapp_test.module.UserModule;
import com.laetienda.webapp_test.module.UserModuleImplementation;
import com.laetienda.webapp_test.service.GroupTestService;
import com.laetienda.webapp_test.service.GroupTestServiceImplementation;
import com.laetienda.webapp_test.service.UserTestService;
import com.laetienda.webapp_test.service.UserTestServiceImplementation;
import com.laetienda.utils.service.api.GroupApi;
import com.laetienda.utils.service.api.GroupApiImplementation;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class UsuarioTestConfiguration {

	@Bean
	public UserModule getUserTestModule(){
		return new UserModuleImplementation();
	}

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
