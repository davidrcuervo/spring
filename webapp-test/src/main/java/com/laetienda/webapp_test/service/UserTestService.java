package com.laetienda.webapp_test.service;

import com.laetienda.model.user.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

//@Service
public interface UserTestService {
    public UserTestService setPort(String port);
    public UserTestService setPort(Integer port);
    public ResponseEntity<Usuario> findByUsername(String username, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<Usuario> findByUsername(String username) throws HttpClientErrorException;
    public ResponseEntity<Usuario> create(Usuario user) throws HttpClientErrorException;
    ResponseEntity<String> delete(String username, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<String> delete(String username) throws HttpClientErrorException;
    public ResponseEntity<String> emailValidation(String token, String loginUsername, String password) throws HttpClientErrorException;
    ResponseEntity<String> login(String username, String password) throws HttpClientErrorException;
    ResponseEntity<String> login(String sessionId) throws HttpClientErrorException;
    ResponseEntity<String> logout(String sessionId) throws HttpClientErrorException;
    void session() throws HttpClientErrorException;
}
