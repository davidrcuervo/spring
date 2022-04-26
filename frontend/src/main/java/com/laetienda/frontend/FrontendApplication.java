package com.laetienda.frontend;

import com.laetienda.frontend.repository.FormRepository;
import com.laetienda.frontend.repository.FormRepositoryImpl;
import com.laetienda.frontend.repository.ThankyouPageRepoImpl;
import com.laetienda.frontend.repository.ThankyouPageRepository;
import com.laetienda.frontend.service.ThankyouPageService;
import com.laetienda.frontend.service.ThankyouPageServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FrontendApplication {

	@Bean(name="formRepository")
	public FormRepository getFormRepository(){
		return new FormRepositoryImpl();
	}

	@Bean
	public ThankyouPageRepository getThankyouPageRepository(){
		return new ThankyouPageRepoImpl();
	}

	@Bean
	public ThankyouPageService getThankyouPageService(){
		return new ThankyouPageServiceImpl(getThankyouPageRepository());
	}

	public static void main(String[] args) {
		SpringApplication.run(FrontendApplication.class, args);
	}

}
