package com.laetienda.frontend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.frontend.lib.FormNotValidException;
import com.laetienda.lib.model.Mistake;
import com.laetienda.model.user.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class RestClientServiceImpl implements RestClientService {
    final private static Logger log = LoggerFactory.getLogger(RestClientServiceImpl.class);
    @Autowired
    private RestTemplate restclient;

    public <T> T post(String url, Object data, Class<T> clazz){

        T result = null;

        try{
            ResponseEntity<T> response = restclient.postForEntity(url, data, clazz);
            if(response.getStatusCode() == HttpStatus.OK){
                result = response.getBody();
            }else{
                log.debug("response code: {}", response.getStatusCode());
            }

        }catch(HttpClientErrorException e){
            throw new FormNotValidException(e);
        }catch (Exception e){
            throw new FormNotValidException(e);
        }

        return result;
    }

    @Override
    public <T> T find(String apiurl, String id, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> T delete(String apiurl, Object data, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> T modify(String apiurl, Object data, Class<T> clazz) {
        return null;
    }
}
