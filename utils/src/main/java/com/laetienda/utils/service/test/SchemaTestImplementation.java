package com.laetienda.utils.service.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.model.schema.DbItem;
import com.laetienda.utils.service.api.SchemaApi;
import com.laetienda.utils.service.api.UserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaTestImplementation implements SchemaTest {
    private final static Logger log = LoggerFactory.getLogger(SchemaTestImplementation.class);

    @Autowired private SchemaApi schemaApi;
    @Autowired private UserApi userApi;
    @Autowired private ObjectMapper jsonMapper;

    @Value("${admuser.username}")
    private String admuser;

    private String admuserPassword;

    @Override
    public ResponseEntity<String> helloAll() throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::helloAll");
        ResponseEntity<String> response = schemaApi.helloAll();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Hello World!!", response.getBody());
        return response;
    }

    @Override
    public ResponseEntity<String> helloUser(String username, String password) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::helloUser $username: {}", username);
        HttpClientErrorException ex;
        ResponseEntity<String> response;

        ex = assertThrows(HttpClientErrorException.class, () -> {
            schemaApi.helloUser();
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());

        String session = loginSession(admuser, admuserPassword);
        response = ((SchemaApi)schemaApi.setSessionId(session)).helloUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Hello " + username, response.getBody());

        response = ((UserApi)userApi.setSessionId(session)).logout();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ex = assertThrows(HttpClientErrorException.class, () -> {
            ((SchemaApi)schemaApi.setSessionId(session)).helloUser();
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());

        return response;
    }

    @Override
    public ResponseEntity<String> helloValidateUser(String username, String password) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::helloValidateUser. $username: {}", username);

        ResponseEntity<String> response;

        response = ((UserApi)userApi.setCredentials(username, password)).login();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        String session = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];

        response = ((SchemaApi)schemaApi.setSessionId(session)).helloValidatedUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Hello " + username, response.getBody());

        ((UserApi)userApi.setSessionId(session)).logout();

        return response;
    }

    @Override
    public <T> ResponseEntity<T> create(Class<T> clazz, DbItem item) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::create $clazzName: {}", clazz.getName());
        String session = loginSession(admuser, admuserPassword);

        ResponseEntity<T> response = ((SchemaApi)schemaApi.setSessionId(session))
                .create(clazz, item);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        //convert from json string to clazz item
        DbItem itemResp = (DbItem) response.getBody();
        assertTrue(itemResp.getId() > 0);
        assertEquals(admuser, itemResp.getOwner());

        return response;
    }

    @Override
    public <T> ResponseEntity<T> createBadEditor(Class<T> clazz, DbItem item) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::createBadEditor");

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            ResponseEntity<T> response = ((SchemaApi)schemaApi.setCredentials(admuser, admuserPassword))
                .create(clazz, item);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        return null;
    }

    @Override
    public <T> ResponseEntity<T> find(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::find. $class: {}, $key: {}, $value: {}", clazz.getName());

        ResponseEntity<T> response = ((SchemaApi)schemaApi.setCredentials(admuser, admuserPassword))
                .find(clazz, body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response;
    }

    @Override
    public <T> HttpClientErrorException notFound(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::notFound. $class: {}, $key: {}, $value: {}", clazz.getName());

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            ((SchemaApi)schemaApi.setCredentials(admuser, admuserPassword))
                    .find(clazz, body);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        return ex;
    }

    @Override
    public <T> ResponseEntity<String> delete(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::delete. $class: {}, $key: {}, $value: {}", clazz.getName());

        ResponseEntity<String> response = ((SchemaApi)schemaApi.setCredentials(admuser, admuserPassword))
                .delete(clazz, body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(Boolean.parseBoolean(response.getBody()));
        return null;
    }

    @Override
    public SchemaTest setPort(Integer port) {
        schemaApi.setPort(port);
        return this;
    }

    @Override
    public SchemaTest setPort(String port) {
        schemaApi.setPort(port);
        return this;
    }

    @Override
    public SchemaTest setAdmuserPassword(String password) {
        this.admuserPassword = password;
        return this;
    }

    private String loginSession (String username, String password){
        ResponseEntity<String> response = ((UserApi)userApi.setCredentials(username, password)).login();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        String session = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];
        log.trace("SCHEMA_TEST::helloUser $session: {}", session);
        return session;
    }

    private void endSession(String session){
        userApi.endSession();userApi.startSession();
    }
}
