package com.laetienda.frontend;

import com.laetienda.frontend.repository.FormRepository;
import com.laetienda.frontend.repository.FormRepositoryImpl;
import com.laetienda.frontend.repository.ThankyouPageRepoImpl;
import com.laetienda.frontend.repository.ThankyouPageRepository;
import com.laetienda.frontend.service.ThankyouPageService;
import com.laetienda.frontend.service.ThankyouPageServiceImpl;
import com.laetienda.utils.lib.CustomRestAuthenticationProvider;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.utils.service.RestClientServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FrontendApplication {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

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

	@Bean
	public RestClientService getUserService(){
		return new RestClientServiceImpl();
	}

	@Bean
	public CustomRestAuthenticationProvider customRestAuthenticationProvider(){
		return new CustomRestAuthenticationProvider();
	}
	public static void main(String[] args) {
		SpringApplication.run(FrontendApplication.class, args);
	}

}
