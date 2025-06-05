package com.laetienda.webapp_test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.model.schema.DbItem;
import com.laetienda.utils.service.api.SchemaApi;
import com.laetienda.utils.service.api.UserApiDeprecated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

//@Service
public class SchemaTestImplementation implements SchemaTest {
    private final static Logger log = LoggerFactory.getLogger(SchemaTestImplementation.class);

    @Autowired private SchemaApi schemaApi;
    @Autowired private UserApiDeprecated userApiDeprecated;
    @Autowired private ObjectMapper jsonMapper;

    @Value("${admuser.username}")
    private String admuser;

    @Value("${admuser.password}")
    private String admuserPassword;

//    @Override
//    public ResponseEntity<String> helloAll() throws HttpClientErrorException {
//        log.debug("SCHEMA_TEST::helloAll");
//        ResponseEntity<String> response = schemaApi.helloAll();
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals("Hello World!!", response.getBody());
//        return response;
//    }

//    @Override
//    public ResponseEntity<String> helloUser(String username, String password) throws HttpClientErrorException {
//        log.debug("SCHEMA_TEST::helloUser $username: {}", username);
//        HttpClientErrorException ex;
//        ResponseEntity<String> response;
//
//        ex = assertThrows(HttpClientErrorException.class, () -> {
//            schemaApi.helloUser();
//        });
//        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
//
//        String session = loginSession(admuser, admuserPassword);
//        response = ((SchemaApi)schemaApi.setSessionId(session)).helloUser();
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals("Hello " + username, response.getBody());
//
//        response = ((UserApiDeprecated)userApiDeprecated.setSessionId(session)).logout();
//        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//
//        ex = assertThrows(HttpClientErrorException.class, () -> {
//            ((SchemaApi)schemaApi.setSessionId(session)).helloUser();
//        });
//        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
//
//        return response;
//    }

//    @Override
//    public ResponseEntity<String> login(String username, String password) throws HttpClientErrorException {
//        log.debug("SCHEMA_TEST::helloValidateUser. $username: {}", username);
//
//        ResponseEntity<String> response;
//
//        response = ((UserApiDeprecated)userApiDeprecated.setCredentials(username, password)).login();
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//
//        String session = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];
//
//        response = ((SchemaApi)schemaApi.setSessionId(session)).login();
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals("Hello " + username, response.getBody());
//
//        ((UserApiDeprecated)userApiDeprecated.setSessionId(session)).logout();
//
//        return response;
//    }

    @Override
    public ResponseEntity<String> startSession(String username, String password) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::startSession $username: {}", username);

        ResponseEntity<String> resp =
                ((SchemaApi)schemaApi.setCredentials(username, password))
                        .startSession();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Succesfull log in by " + username, resp.getBody());
        assertNotNull(schemaApi.getSession());
        return resp;
    }

    @Override
    public ResponseEntity<String> endSession() throws HttpClientErrorException {
        log.trace("SCHEMA_TEST::endSession");

        ResponseEntity<String> resp = schemaApi.endSession();
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        assertNull(schemaApi.getSession());
        return null;
    }

    @Override
    public <T> ResponseEntity<T> create(Class<T> clazz, DbItem item) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::create $clazzName: {}", clazz.getName());
//        String session = loginSession(admuser, admuserPassword);
//        ResponseEntity<T> response = ((SchemaApi)schemaApi.setSessionId(session))
//                .create(clazz, item);

        ResponseEntity<T> response = schemaApi.create(clazz, item);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        //convert from json string to clazz item
        T itemResp = response.getBody();
        DbItem dbItem = (DbItem) response.getBody();
//        ItemTypeA dbItem = (ItemTypeA)response.getBody();
        assertTrue(dbItem.getId() > 0);
        assertEquals(admuser, dbItem.getOwner());

        return response;
    }

    @Override
    public <T> ResponseEntity<T> createBadEditor(Class<T> clazz, DbItem item) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::createBadEditor");

//        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
//            ResponseEntity<T> response = ((SchemaApi)schemaApi.setCredentials(admuser, admuserPassword))
//                .create(clazz, item);
//        });
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            ResponseEntity<T> response = schemaApi.create(clazz, item);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        return null;
    }

    @Override
    public <T> ResponseEntity<T> find(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::find. $class: {}, $key: {}, $value: {}", clazz.getName());

//        ResponseEntity<T> response = ((SchemaApi)schemaApi.setCredentials(admuser, admuserPassword))
//                .find(clazz, body);
        ResponseEntity<T> response = schemaApi.find(clazz, body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response;
    }

    @Override
    public <T> ResponseEntity<T> findById(Class<T> clazz, Long id) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::findById. $class: {}, $id: {}", clazz.getName(), id);
        ResponseEntity<T> response = schemaApi.findById(clazz, id);

        //test result
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(((DbItem)response.getBody()).getId() > 0);
        return response;
    }

    @Override
    public <T> HttpClientErrorException notFound(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::notFound. $class: {}, $key: {}, $value: {}", clazz.getName());

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
//            ((SchemaApi)schemaApi.setCredentials(admuser, admuserPassword))
//                    .find(clazz, body);
            schemaApi.find(clazz, body);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        return ex;
    }

    @Override
    public <T> ResponseEntity<String> delete(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::delete. $class: {}, $key: {}, $value: {}", clazz.getName());

//        ResponseEntity<String> response = ((SchemaApi)schemaApi.setCredentials(admuser, admuserPassword))
//                .delete(clazz, body);
        ResponseEntity<String> response = schemaApi.delete(clazz, body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(Boolean.parseBoolean(response.getBody()));
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            schemaApi.find(clazz, body);
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        return response;
    }

    @Override
    public <T> ResponseEntity<String> deleteById(Class<T> clazz, Long id) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::deleteById. $class: {}, $id: {}", clazz.getName(), id);
        ResponseEntity<String> response = schemaApi.deleteById(clazz, id);

        //test response is fine
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(Boolean.parseBoolean(response.getBody()));
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            schemaApi.findById(clazz, id);
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        return response;
    }

    @Override
    public <T> ResponseEntity<T> update(Class<T> clazz, DbItem item) throws HttpClientErrorException {
        log.debug("SCHEMA_TEST::update $clazzName: {}", clazz.getName());

        ResponseEntity<T> response = schemaApi.update(clazz, item);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        DbItem itemResp = (DbItem) response.getBody();
        assertEquals(item.getId(), itemResp.getId());
        return response;
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

//    private String loginSession (String username, String password){
//        ResponseEntity<String> response = ((UserApiDeprecated)userApiDeprecated.setCredentials(username, password)).login();
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//
//        String session = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];
//        log.trace("SCHEMA_TEST::helloUser $session: {}", session);
//        return session;
//    }
//
//    private void endSession(String session){
//        userApiDeprecated.endSession();userApiDeprecated.startSession();
//    }
}
