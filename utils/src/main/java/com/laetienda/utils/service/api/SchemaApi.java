package com.laetienda.utils.service.api;

import com.laetienda.model.schema.DbItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public interface SchemaApi extends ApiClientService{
    ResponseEntity<String> helloAll() throws HttpClientErrorException;
    ResponseEntity<String> helloUser() throws HttpClientErrorException;
    ResponseEntity<String> helloValidatedUser() throws HttpClientErrorException;
    ResponseEntity<DbItem> create(DbItem item) throws HttpClientErrorException;
}
