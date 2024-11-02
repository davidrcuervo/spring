package com.laetienda.utils.service.api;

import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public interface UserApi extends ApiClientService{

    public ResponseEntity<UsuarioList> findAll() throws HttpClientErrorException;
    public ResponseEntity<Usuario> findByUsername(String username) throws HttpClientErrorException;
    public ResponseEntity<Usuario> create(Usuario usuario) throws HttpClientErrorException;
    public ResponseEntity<Usuario> update(Usuario user) throws HttpClientErrorException;
    public ResponseEntity<String> delete(String username) throws HttpClientErrorException;
    public ResponseEntity<GroupList> authenticateUser(Usuario user) throws HttpClientErrorException;
    public ResponseEntity<String> emailValidation(String token) throws HttpClientErrorException;

//    public UserApi setCredentials(String loginUsername, String password);
//    public UserApi setPort(Integer port);
//    public UserApi setPort(String port);
}
