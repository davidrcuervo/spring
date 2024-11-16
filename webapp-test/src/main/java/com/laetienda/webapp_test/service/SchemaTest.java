package com.laetienda.webapp_test.service;

import com.laetienda.model.schema.DbItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

public interface SchemaTest extends WebappTest {
//    ResponseEntity<String> helloAll() throws HttpClientErrorException;
//    ResponseEntity<String> helloUser(String username, String password) throws HttpClientErrorException;
//    ResponseEntity<String> login(String username, String password) throws HttpClientErrorException;
    ResponseEntity<String> startSession(String username, String password) throws HttpClientErrorException;
    ResponseEntity<String> endSession() throws HttpClientErrorException;
    <T> ResponseEntity<T> create (Class<T> clazz, DbItem item) throws HttpClientErrorException;
    <T> ResponseEntity<T> createBadEditor(Class<T> clazz, DbItem item) throws HttpClientErrorException;
    <T> ResponseEntity<T> find (Class<T> clazz, Map<String, String> body) throws HttpClientErrorException;
    <T> HttpClientErrorException notFound (Class<T> clazz, Map<String, String> body) throws HttpClientErrorException;
    <T> ResponseEntity<String> delete(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException;
}
