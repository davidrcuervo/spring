package com.laetienda.utils.service.test;

import com.laetienda.model.schema.DbItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public interface SchemaTest extends WebappTest {
    ResponseEntity<String> helloAll() throws HttpClientErrorException;
    ResponseEntity<String> helloUser(String username, String password) throws HttpClientErrorException;
    ResponseEntity<String> helloValidateUser(String username, String password) throws HttpClientErrorException;
    ResponseEntity<String> create (String clazzName, DbItem item) throws HttpClientErrorException;
    ResponseEntity<DbItem> createBadEditor(String clazzName, DbItem item) throws HttpClientErrorException;
}
