package com.laetienda.utils.service.api;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public interface ApiClientService {

    RestClient getRestClient();
    ApiClientService setSessionId(String sessionId);
    ApiClientService setCredentials(String loginUsername, String password);
    ApiClientService setPort(String port);
    ApiClientService setPort(Integer port);
    String getPort();
}