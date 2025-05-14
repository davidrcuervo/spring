package com.laetienda.lib.service;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

public interface CustomRestClient {

    ClientHttpRequestInterceptor oauth2Interceptor(OAuth2AuthorizedClientManager clientManager);
}
