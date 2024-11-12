package com.laetienda.utils.service.api;

import com.laetienda.model.schema.DbItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public interface SchemaApi extends ApiClient {
    ResponseEntity<String> helloAll() throws HttpClientErrorException;
    ResponseEntity<String> helloUser() throws HttpClientErrorException;
    ResponseEntity<String> helloValidatedUser() throws HttpClientErrorException;
    ResponseEntity<String> create(String clazzName, DbItem item) throws HttpClientErrorException;
}
