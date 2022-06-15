package com.laetienda.usuario;

import com.laetienda.lib.service.TestRestClient;
import com.laetienda.lib.service.TestRestClientImpl;
import com.laetienda.usuario.lib.LdapDn;
import com.laetienda.usuario.lib.LdapDnImpl;
import com.laetienda.usuario.lib.LdapDnImplB;
import com.laetienda.usuario.repository.GroupRepoImpl;
import com.laetienda.usuario.repository.GroupRepository;
import com.laetienda.usuario.repository.UserRepoImpl;
import com.laetienda.usuario.repository.UserRepository;
import com.laetienda.usuario.service.GroupService;
import com.laetienda.usuario.service.GroupServiceImpl;
import com.laetienda.usuario.service.UserService;
import com.laetienda.usuario.service.UserServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableLdapRepositories(basePackages = "com.laetienda.usuario.repository")
public class UsuarioApplication {

	@Bean
	public LdapDn getDnBuilder(){
		return new LdapDnImplB();
	}

	@Bean
	public UserService getUserService(){
//		return new UserServiceImpl(getUserRepository());
		return new UserServiceImpl();
	}

	@Bean
	public UserRepository getUserRepository(){
		return new UserRepoImpl();
	}

	@Bean
	public GroupRepository getGroupRepository(){return new GroupRepoImpl(); }

	@Bean
	public GroupService getGroupService(){
		return new GroupServiceImpl();
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(UsuarioApplication.class, args);
	}

}
