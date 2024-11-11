package com.laetienda.utils.service.api;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public interface ApiClient {

    RestClient getRestClient();
    ApiClient setSessionId(String sessionId);
    ApiClient setCredentials(String loginUsername, String password);
    ApiClient setPort(String port);
    ApiClient setPort(Integer port);
    String getPort();
    String getUsername();
    String getSession();
}