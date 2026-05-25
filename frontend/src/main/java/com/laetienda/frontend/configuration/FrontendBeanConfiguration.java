package com.laetienda.frontend.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.frontend.repository.FormRepository;
import com.laetienda.frontend.repository.FormRepositoryImpl;
import com.laetienda.frontend.repository.ThankyouPageRepoImpl;
import com.laetienda.frontend.repository.ThankyouPageRepository;
import com.laetienda.frontend.service.ThankYouPageService;
import com.laetienda.frontend.service.ThankYouPageServiceImpl;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.lib.service.ToolBoxServiceImpl;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.utils.lib.UtilsBoxImplementation;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.utils.service.RestClientServiceImpl;
import com.laetienda.utils.service.api.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FrontendBeanConfiguration {

	private final RestTemplateBuilder restTemplateBuilder;
	private final RestClient client;
    private final Environment env;
	private final ObjectMapper json;
	private final OAuth2AuthorizedClientManager authorizedClientManager;

	public FrontendBeanConfiguration(
			OAuth2AuthorizedClientManager authorizedClientManager,
			RestTemplateBuilder restTemplateBuilder,
			RestClient restClient,
			Environment environment,
			ObjectMapper objectMapper
	) {
		this.authorizedClientManager = authorizedClientManager;
		this.restTemplateBuilder = restTemplateBuilder;
		this.client = restClient;
        this.env = environment;
		this.json = objectMapper;
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
	public ThankYouPageService getThankyouPageService(){
		return new ThankYouPageServiceImpl(getThankyouPageRepository());
	}

	@Bean
	public RestClientService getUserService(){
		return new RestClientServiceImpl();
	}

	@Bean
	public ApiUser getApiUser() {
		return new ApiUserImplementation(client, env, json);
	}

	@Bean
	public ApiCompany getApiCompany() {
		return new ApiCompanyImplementation(client);
	}

    @Bean
    public ToolBoxService getToolBoxService(){
        return new ToolBoxServiceImpl();
    }

	@Bean
	public UtilsBox getUtilsBox(ApiUser apiUser) {
		return new UtilsBoxImplementation(apiUser,  authorizedClientManager);
	}
}
