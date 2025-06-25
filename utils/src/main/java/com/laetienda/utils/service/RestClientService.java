package com.laetienda.utils.service;

import com.laetienda.lib.exception.CustomRestClientExceptionDeprecated;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface RestClientService {

    @Deprecated
    public <T> T post(String apiurl, Object data, Class<T> clazz) throws CustomRestClientExceptionDeprecated;

    @Deprecated
    public <T> T find(String apiurl, Class<T> clazz, Map<String, String>params) throws CustomRestClientExceptionDeprecated;

    @Deprecated
    public <T> T findall(String apiurl, Class<T> clazz) throws CustomRestClientExceptionDeprecated;

    @Deprecated
    public <T> T delete(String apiurl, Class<T> clazz, Map<String, String> params) throws CustomRestClientExceptionDeprecated;

    @Deprecated
    public <T> T delete(String apiurl, Class<T> clazz) throws CustomRestClientExceptionDeprecated;

    @Deprecated
    public <T> T modify(String apiurl, Object data, Class<T> clazz) throws CustomRestClientExceptionDeprecated;
    <T> T send(String apiurl, HttpMethod httpMethod, Object data, Class<T> clazz, Map<String, String> params) throws CustomRestClientExceptionDeprecated;
}
