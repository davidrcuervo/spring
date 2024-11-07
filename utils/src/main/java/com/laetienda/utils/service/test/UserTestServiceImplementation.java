package com.laetienda.utils.service.test;

import com.laetienda.model.user.Usuario;
import com.laetienda.utils.service.api.UserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;

public class UserTestServiceImplementation implements UserTestService {
    private final static Logger log = LoggerFactory.getLogger(UserTestServiceImplementation.class);

    @Autowired
    private UserApi userApi;

    @Value("${admuser.username}")
    private String admuser;

    private String port;
    private String admuserPassword;

    @Override
    public UserTestService setPort(String port) {
        this.port = port;
        return this;
    }

    @Override
    public UserTestService setPort(Integer port) {
        setPort(Integer.toString(port));
        return this;
    }

    @Override
    public UserTestService setAdmuserPassword(String password) {
        this.admuserPassword = password;
        return this;
    }

    @Override
    public ResponseEntity<Usuario> findByUsername(String username, String loginUsername, String password) throws HttpClientErrorException {
        ResponseEntity<Usuario> response =
                ((UserApi)userApi.setPort(port).setCredentials(loginUsername, password))
                .findByUsername(username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response;
    }

    @Override
    public ResponseEntity<Usuario> findByUsername(String username) throws HttpClientErrorException {
        return findByUsername(username, admuser, admuserPassword);
    }

    private void findUserNotExists(String username){
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            findByUsername(username);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Override
    public ResponseEntity<Usuario> create(Usuario user) throws HttpClientErrorException {
        findUserNotExists(user.getUsername());

        ResponseEntity<Usuario> response = ((UserApi)userApi.setPort(port)).create(user);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getEncToken());
        assertFalse(response.getBody().getEncToken().isBlank());

        findByUsername(user.getUsername());
        return response;
    }

    @Override
    public ResponseEntity<String> delete(String username, String loginUsername, String password) throws HttpClientErrorException {
        findByUsername(username);

        ResponseEntity<String> response =
                ((UserApi)userApi.setPort(port).setCredentials(loginUsername, password))
                .delete(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("true", response.getBody());

        findUserNotExists(username);
        return response;
    }

    @Override
    public ResponseEntity<String> delete(String username) throws HttpClientErrorException {
        return delete(username, admuser, admuserPassword);
    }

    @Override
    public ResponseEntity<String> emailValidation(String token, String loginUsername, String password) throws HttpClientErrorException {
        ResponseEntity<String> response =
                ((UserApi)userApi.setPort(port).setCredentials(loginUsername, password))
                .emailValidation(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(Boolean.parseBoolean(response.getBody()));
        return response;
    }

    @Override
    public ResponseEntity<String> login(String username, String password) throws HttpClientErrorException {
        ResponseEntity<String> response =
                ((UserApi)userApi.setPort(port).setCredentials(username, password))
                        .login();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response;
    }

    @Override
    public ResponseEntity<String> login(String sessionId) throws HttpClientErrorException {
        ResponseEntity<String> response =
                ((UserApi)userApi.setPort(port).setSessionId(sessionId))
                        .login();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response;
    }
}