package com.laetienda.utils.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

public interface ApiSchema extends ApiClient {
    ResponseEntity<String> helloAll() throws HttpClientErrorException;
    ResponseEntity<String> helloUser() throws HttpClientErrorException;
    ResponseEntity<String> login() throws HttpClientErrorException;
    ResponseEntity<String> startSession() throws HttpClientErrorException;
    ResponseEntity<String> endSession() throws HttpClientErrorException;
    <T> ResponseEntity<T> create(Class<T> clazz, DbItem item) throws NotValidCustomException;
    <T> ResponseEntity<T> find(Class<T> clazz, Map<String, String> body) throws NotValidCustomException;
    <T> ResponseEntity<T> findById(Class<T> clazz, Long id) throws NotValidCustomException;
    <T> ResponseEntity<String> delete(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException;
    <T> ResponseEntity<String> deleteById(Class<T> clazz, Long id) throws NotValidCustomException;
    <T> ResponseEntity<T> update(Class<T> clazz, DbItem item) throws HttpClientErrorException;
}
