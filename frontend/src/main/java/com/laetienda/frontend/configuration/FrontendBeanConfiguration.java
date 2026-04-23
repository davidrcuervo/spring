package com.laetienda.frontend.configuration;

import com.laetienda.frontend.repository.FormRepository;
import com.laetienda.frontend.repository.FormRepositoryImpl;
import com.laetienda.frontend.repository.ThankyouPageRepoImpl;
import com.laetienda.frontend.repository.ThankyouPageRepository;
import com.laetienda.frontend.service.ThankyouPageService;
import com.laetienda.frontend.service.ThankyouPageServiceImpl;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.utils.lib.UtilsBoxImplementation;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.utils.service.RestClientServiceImpl;
import com.laetienda.utils.service.api.ApiUser;
import com.laetienda.utils.service.api.ApiUserImplementation;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FrontendBeanConfiguration {

	private final RestTemplateBuilder restTemplateBuilder;
	private final RestClient client;
	private final OAuth2AuthorizedClientManager authorizedClientManager;

	public FrontendBeanConfiguration(
			OAuth2AuthorizedClientManager authorizedClientManager,
			RestTemplateBuilder restTemplateBuilder,
			RestClient restClient
	) {
		this.authorizedClientManager = authorizedClientManager;
		this.restTemplateBuilder = restTemplateBuilder;
		this.client = restClient;
	}

    @Bean
	public RestTemplate restTemplate() {
		return restTemplateBuilder.build();
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
	public ApiUser getApiUser() {
		return new ApiUserImplementation(client);
	}

	@Bean
	public UtilsBox getUtilsBox(ApiUser apiUser) {
		return new UtilsBoxImplementation(apiUser,  authorizedClientManager);
	}
}
