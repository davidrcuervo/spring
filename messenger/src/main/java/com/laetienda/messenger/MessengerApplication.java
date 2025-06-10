package com.laetienda.messenger;

import com.laetienda.utils.lib.CustomRestAuthenticationProvider;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.utils.service.RestClientServiceImpl;
import com.laetienda.utils.service.api.MessengerApi;
import com.laetienda.utils.service.api.MessengerApiImplementation;
import com.laetienda.utils.service.api.UserApiDeprecated;
import com.laetienda.utils.service.api.UserApiDeprecatedImplementation;
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

	@Bean
	public UserApiDeprecated getUserApi(){
		return new UserApiDeprecatedImplementation();
	}

	@Bean
	public MessengerApi getMessengerApi(){
		return new MessengerApiImplementation();
	}

	@Bean
	public CustomRestAuthenticationProvider getAuthenticationProvider(){
		return new CustomRestAuthenticationProvider();
	}

	public static void main(String[] args) {
		SpringApplication.run(MessengerApplication.class, args);
	}

}
