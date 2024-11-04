package com.laetienda.utils.service.api;

import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public interface UserApi extends ApiClientService{

    ResponseEntity<UsuarioList> findAll() throws HttpClientErrorException;
    ResponseEntity<Usuario> findByUsername(String username) throws HttpClientErrorException;
    ResponseEntity<Usuario> create(Usuario usuario) throws HttpClientErrorException;
    ResponseEntity<Usuario> update(Usuario user) throws HttpClientErrorException;
    ResponseEntity<String> delete(String username) throws HttpClientErrorException;
    ResponseEntity<GroupList> authenticateUser(Usuario user) throws HttpClientErrorException;
    ResponseEntity<String> emailValidation(String token) throws HttpClientErrorException;

//    public UserApi setCredentials(String loginUsername, String password);
//    public UserApi setPort(Integer port);
//    public UserApi setPort(String port);
}
