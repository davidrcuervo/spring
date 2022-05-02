package com.laetienda.usuario;

import com.laetienda.usuario.repository.UserRepoImpl;
import com.laetienda.usuario.repository.UserRepository;
import com.laetienda.usuario.service.UserService;
import com.laetienda.usuario.service.UserServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;

@SpringBootApplication
@EnableLdapRepositories(basePackages = "com.laetienda.usuario.repository")
public class UsuarioApplication {

	@Bean
	public UserService getUserService(){
//		return new UserServiceImpl(getUserRepository());
		return new UserServiceImpl();
	}

	@Bean
	public UserRepository getUserRepository(){
		return new UserRepoImpl();
	}
	public static void main(String[] args) {
		SpringApplication.run(UsuarioApplication.class, args);
	}

}
