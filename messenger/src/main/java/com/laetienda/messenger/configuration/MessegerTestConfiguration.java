package com.laetienda.messenger.configuration;

import com.laetienda.lib.service.TestRestClient;
import com.laetienda.lib.service.TestRestClientImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MessegerTestConfiguration {
    @Bean
    public TestRestClient getTestRestClient(){
        return new TestRestClientImpl();
    }
}
