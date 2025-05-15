package com.laetienda.utils.service.api;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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
    ResponseEntity<String> startSession(String loginAddres, String logoutAddress) throws HttpClientErrorException;
    ResponseEntity<String> endSession(String logoutAddress) throws HttpClientErrorException;
//    ApiClient startSession() throws HttpClientErrorException;
//    ApiClient endSession() throws HttpClientErrorException;
}