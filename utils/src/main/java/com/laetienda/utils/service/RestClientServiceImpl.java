package com.laetienda.utils.service;

import com.laetienda.lib.exception.CustomRestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RestClientServiceImpl implements RestClientService {
    final private static Logger log = LoggerFactory.getLogger(RestClientServiceImpl.class);
    @Autowired
    private RestTemplate restclient;

    @Autowired
    private HttpSession session;

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
            throw new CustomRestClientException(e);
        }catch (Exception e){
            throw new CustomRestClientException(e);
        }

        return result;
    }

    @Override
    public <T> T find(String apiurl, Class<T> clazz, Map<String, String> params) throws CustomRestClientException{
        return send(apiurl, HttpMethod.GET, null, clazz, params);
    }

    @Override
    public <T> T findall(String apiurl, Class<T> clazz) {
        return send(apiurl, HttpMethod.GET, null, clazz, null);
    }

    @Override
    public <T> T delete(String apiurl, Class<T> clazz, Map<String, String> params) throws CustomRestClientException {
        return send(apiurl, HttpMethod.DELETE, null, clazz, params);
    }

    @Override
    public <T> T delete(String apiurl, Class<T> clazz) throws CustomRestClientException{
        return send(apiurl, HttpMethod.DELETE, null, clazz, null);
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
    @Override
    public <T> T send(String apiurl, HttpMethod httpMethod, Object data, Class<T> clazz, Map<String, String> params) throws CustomRestClientException{
        T result = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", String.format("SESSION=%s", Base64.getEncoder().encodeToString(session.getId().getBytes())));
        log.trace("SESSION: {}", Base64.getEncoder().encodeToString(session.getId().getBytes()));
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
                throw new CustomRestClientException(new IOException("Wrong response status"));
            }
        }catch(HttpClientErrorException e){
            throw new CustomRestClientException(e);
        }catch (Exception e){
            throw new CustomRestClientException(e);
        }

        return result;
    }
}
