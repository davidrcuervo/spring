package com.laetienda.utils.service.api;

import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Component
public class UserApiDeprecatedKcImplementation implements UserApiDeprecated {

    @Autowired private Environment env;

    private final RestClient client;

    public UserApiDeprecatedKcImplementation(RestClient restClient){
        this.client = restClient;
    }

    @Override
    public ResponseEntity<UsuarioList> findAll() throws HttpClientErrorException {
        return null;
    }

    @Override
    public ResponseEntity<Usuario> findByUsername(String username) throws HttpClientErrorException {
        return null;
    }

    @Override
    public ResponseEntity<Usuario> create(Usuario usuario) throws HttpClientErrorException {
        return null;
    }

    @Override
    public ResponseEntity<Usuario> update(Usuario user) throws HttpClientErrorException {
        return null;
    }

    @Override
    public ResponseEntity<String> delete(String username) throws HttpClientErrorException {
        return null;
    }

    @Override
    public ResponseEntity<GroupList> authenticateUser(Usuario user) throws HttpClientErrorException {
        return null;
    }

    @Override
    public ResponseEntity<String> login() throws HttpClientErrorException {
        String address = env.getProperty("api.kcUser.login.uri", "login");
        return client.get().uri(address)
                .attributes(clientRegistrationId("webapp"))
                .retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<String> logout() throws HttpClientErrorException {
        return null;
    }

    @Override
    public ResponseEntity<String> emailValidation(String token) throws HttpClientErrorException {
        return null;
    }

    @Override
    public ResponseEntity<String> startSession() throws HttpClientErrorException {
        return null;
    }

    @Override
    public ResponseEntity<String> endSession() throws HttpClientErrorException {
        return null;
    }

    @Override
    public RestClient getRestClient() {
        return null;
    }

    @Override
    public ApiClient setSessionId(String sessionId) {
        return null;
    }

    @Override
    public ApiClient setCredentials(String loginUsername, String password) {
        return null;
    }

    @Override
    public ApiClient setPort(String port) {
        return null;
    }

    @Override
    public ApiClient setPort(Integer port) {
        return null;
    }

    @Override
    public String getPort() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public String getSession() {
        return "";
    }

    @Override
    public ResponseEntity<String> startSession(String loginAddres, String logoutAddress) throws HttpClientErrorException {
        return null;
    }

    @Override
    public ResponseEntity<String> endSession(String logoutAddress) throws HttpClientErrorException {
        return null;
    }
}
