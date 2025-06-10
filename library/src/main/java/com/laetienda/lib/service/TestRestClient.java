package com.laetienda.lib.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface TestRestClient {

    /**
     *
     * @param apiurl Not Nullable
     * @param port Not Nullable
     * @param httpMethod Not Nullable
     * @param data Nullable, it creates entity with only json and authentication headers
     * @param clazz Not Nullable
     * @param params Nullable, it will create a params with only port
     * @param username Nullable, it will send not credentials
     * @param password Nullable, it will send not credentials
     * @return
     * @param <T>
     */
    <T> ResponseEntity<T> send(String apiurl, int port, HttpMethod httpMethod, Object data, Class<T> clazz, Map<String, String> params, String username, String password);
}
