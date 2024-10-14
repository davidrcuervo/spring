package com.laetienda.utils.service;

import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

public interface UserAndGroupApiRepository {

    public UserAndGroupApiRepository setPort(Integer port);
    public UserAndGroupApiRepository setPort(String port);

    UserAndGroupApiRepository setCredentials(String loginUsername, String password);

    UserAndGroupApiRepository setSessionId(String sessionId);

    public ResponseEntity<Usuario> findByUsername(String username) throws HttpClientErrorException;

    ResponseEntity<Usuario> update(Usuario user) throws HttpClientErrorException;

    public ResponseEntity<UsuarioList> findAll() throws HttpClientErrorException;

    ResponseEntity<GroupList> authenticateUser(Usuario user) throws HttpClientErrorException;

    public ResponseEntity<Usuario> create(Usuario usuario) throws HttpClientErrorException;

    ResponseEntity<String> delete(String username) throws HttpClientErrorException;
}
