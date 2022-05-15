package com.laetienda.frontend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.frontend.lib.FormNotValidException;
import com.laetienda.lib.model.Mistake;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import jdk.jfr.ContentType;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

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
    public <T> T findall(String apiurl, Class<T> clazz) {
        return clazz.cast(send(apiurl, HttpMethod.GET, null, UsuarioList.class, null));
    }

    @Override
    public <T> T delete(String apiurl, Object data, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> T modify(String apiurl, Object data, Class<T> clazz) {
        return null;
    }

    /**
     *
     * @param apiurl
     * @param httpMethod
     * @param data nullable
     * @param clazz
     * @param params nullable
     * @return
     * @param <T>
     */
    private <T> T send(String apiurl, HttpMethod httpMethod, Object data, Class<T> clazz, Map<String, String> params){
        T result = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity;

        if(data == null){
            entity = new HttpEntity(headers);
        }else{
            entity = new HttpEntity<Object>(data, headers);
        }

        if(params == null){
            params = new HashMap<>();
        }

        try{
            ResponseEntity<T> response = restclient.exchange(apiurl, httpMethod, entity, clazz, params);

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

}
