package com.laetienda.messenger;

import com.laetienda.lib.service.TestRestClient;
import com.laetienda.lib.service.TestRestClientImpl;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.utils.service.RestClientServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MessengerApplication {

	@Bean
	public RestTemplate getRestTemplate(RestTemplateBuilder builder){
		return builder.build();
	}

	@Bean
	public RestClientService getRestClientService(){
		return new RestClientServiceImpl();
	}

	public static void main(String[] args) {
		SpringApplication.run(MessengerApplication.class, args);
	}

}
