package com.laetienda.usuario;

import com.laetienda.usuario.lib.CustomLdapAuthenticationProvider;
import com.laetienda.usuario.lib.LdapDn;
import com.laetienda.usuario.lib.LdapDnImplB;
import com.laetienda.usuario.repository.GroupRepoImpl;
import com.laetienda.usuario.repository.GroupRepository;
import com.laetienda.usuario.repository.UserRepoImpl;
import com.laetienda.usuario.service.GroupService;
import com.laetienda.usuario.service.GroupServiceImpl;
import com.laetienda.usuario.service.UserService;
import com.laetienda.usuario.service.UserServiceImpl;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.utils.service.RestClientServiceImpl;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.lib.service.ToolBoxServiceImpl;
import com.laetienda.utils.service.api.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

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
	public com.laetienda.usuario.repository.UserRepository getUserRepository(){
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

	@Bean
	public CustomLdapAuthenticationProvider getAuthenticationProvider(){
		return new CustomLdapAuthenticationProvider();
	}

	@Bean
	public ToolBoxService getToolBoxService(){
		return new ToolBoxServiceImpl();
	}

	@Bean
	public RestClientService getRestClientService(){
		return new RestClientServiceImpl();
	}

	@Bean
//	@Scope(value="request", proxyMode= ScopedProxyMode.DEFAULT)
	public UserApi getUserAndGroupApiRepository() throws IOException {
		return new UserApiImplementation();
	}

	@Bean
	public MessengerApi getMessengerApi(){
		return new MessengerApiImplementation();
	}

//	@Bean
//	public LdapContextSource createLdapConfig(LdapProperties properties, Environment environment,
//											  ObjectProvider<DirContextAuthenticationStrategy> dirContextAuthenticationStrategy) {
//		LdapAutoConfiguration config = new LdapAutoConfiguration();
//		return config.ldapContextSource(properties, environment, dirContextAuthenticationStrategy);
//	}

	public static void main(String[] args) {
		SpringApplication usuarioSpringApplication = new SpringApplication(UsuarioApplication.class);
		usuarioSpringApplication.setAdditionalProfiles("production");
		usuarioSpringApplication.run(args);
//		SpringApplication.run(UsuarioApplication.class, args);
	}

}
