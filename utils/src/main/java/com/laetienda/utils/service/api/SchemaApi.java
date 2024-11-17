package com.laetienda.utils.service.api;

import com.laetienda.model.schema.DbItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

public interface SchemaApi extends ApiClient {
    ResponseEntity<String> helloAll() throws HttpClientErrorException;
    ResponseEntity<String> helloUser() throws HttpClientErrorException;
    ResponseEntity<String> login() throws HttpClientErrorException;
    ResponseEntity<String> startSession() throws HttpClientErrorException;
    ResponseEntity<String> endSession() throws HttpClientErrorException;
    <T> ResponseEntity<T> create(Class<T> clazz, DbItem item) throws HttpClientErrorException;
    <T> ResponseEntity<T> find(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException;
    <T> ResponseEntity<String> delete(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException;
    <T> ResponseEntity<T> update(Class<T> clazz, DbItem item) throws HttpClientErrorException;
}
