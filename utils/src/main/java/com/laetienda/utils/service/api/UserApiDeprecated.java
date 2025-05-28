package com.laetienda.utils.service.api;

import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public interface UserApiDeprecated extends ApiClient {

    @Deprecated
    ResponseEntity<UsuarioList> findAll() throws HttpClientErrorException;
    ResponseEntity<Usuario> findByUsername(String username) throws HttpClientErrorException;
    ResponseEntity<Usuario> create(Usuario usuario) throws HttpClientErrorException;
    ResponseEntity<Usuario> update(Usuario user) throws HttpClientErrorException;
    ResponseEntity<String> delete(String username) throws HttpClientErrorException;
    ResponseEntity<GroupList> authenticateUser(Usuario user) throws HttpClientErrorException;
    ResponseEntity<String> login() throws HttpClientErrorException;
    ResponseEntity<String> logout() throws HttpClientErrorException;
    ResponseEntity<String> emailValidation(String token) throws HttpClientErrorException;
    ResponseEntity<String> startSession() throws HttpClientErrorException;
    ResponseEntity<String> endSession() throws HttpClientErrorException;

//    public UserApiDeprecated setCredentials(String loginUsername, String password);
//    public UserApiDeprecated setPort(Integer port);
//    public UserApiDeprecated setPort(String port);
}
