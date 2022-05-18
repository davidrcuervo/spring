package com.laetienda.frontend.service;

import com.laetienda.frontend.lib.CustomRestClientException;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Usuario;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface RestClientService {

    public <T> T post(String apiurl, Object data, Class<T> clazz) throws CustomRestClientException;
    public <T> T find(String apiurl, Class<T> clazz, Map<String, String>params) throws CustomRestClientException;
    public <T> T findall(String apiurl, Class<T> clazz) throws CustomRestClientException;
    public <T> T delete(String apiurl, Class<T> clazz, Map<String, String> params) throws CustomRestClientException;
    public <T> T delete(String apiurl, Class<T> clazz) throws CustomRestClientException;
    public <T> T modify(String apiurl, Object data, Class<T> clazz) throws CustomRestClientException;
    <T> T send(String apiurl, HttpMethod httpMethod, Object data, Class<T> clazz, Map<String, String> params) throws CustomRestClientException;
}
