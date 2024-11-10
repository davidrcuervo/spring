package com.laetienda.utils.service.test;

import com.laetienda.model.schema.DbItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public interface SchemaTest extends WebappTest {
    ResponseEntity<String> helloAll() throws HttpClientErrorException;
    ResponseEntity<String> helloUser(String username, String password) throws HttpClientErrorException;
    ResponseEntity<String> helloValidateUser(String username, String password) throws HttpClientErrorException;
    ResponseEntity<DbItem> create (DbItem item) throws HttpClientErrorException;
}
