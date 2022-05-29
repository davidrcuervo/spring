package com.laetienda.lib.service;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TestRestClientImpl implements TestRestClient {

    @Autowired
    private TestRestTemplate restclient;

    @Override
    public <T> ResponseEntity<T> send(String apiurl, int port, HttpMethod httpMethod, Object data, Class<T> clazz, Map<String, String> params, String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if(username != null && password != null) {
            headers.add("Authorization", getEncode64(username, password));
        }

        HttpEntity<?> entity;

        if(data == null){
            entity = new HttpEntity(headers);
        }else{
            entity = new HttpEntity<Object>(data, headers);
        }

        if(params == null){
            params = new HashMap<>();
        }
        params.put("port", String.valueOf(port));

        return restclient.exchange(apiurl, httpMethod, entity, clazz, params);
    }

    private String getEncode64(String username, String password){
        String creds = String.format("%s:%s", username, password);
        String result = new String(Base64.encodeBase64String(creds.getBytes()));
        return "Basic " + result;
    }
}
