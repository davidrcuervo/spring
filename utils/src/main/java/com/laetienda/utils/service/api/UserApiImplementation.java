package com.laetienda.utils.service.api;

import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class UserApiImplementation implements UserApi {
    final private static Logger log = LoggerFactory.getLogger(UserApiImplementation.class);

    @Value("${api.usuario.port}")
    private String apiPort;

    @Value("${api.usuario.create}")
    private String createUsuario;

    @Value("${api.usuario.update}")
    private String updateUsuario;

    @Value("${api.usuario.delete}")
    private String deleteUsuario;

    @Value("${api.usuario.find}")
    private String findByUsername;

    @Value("${api.usuario.findAll}")
    private String findAllUsuarios;

    @Value("${api.usuario.findByUsername}")
    private String findUsuarioByUsername;

    @Value("${api.usuario.auth}")
    private String authUserAddress;

    @Value("${api.usuario.emailValidation}")
    private String usuarioEmailValidation;

    @Value("${api.usuario.requestPasswordRecovery}")
    private String requestUsuarioPasswordRecovery;

    @Value("${api.usuario.passwordRecovery}")
    private String passwordRecovery;

    @Autowired
    private ApiClientService api;

    private String port;

    public UserApiImplementation() {
        this.port = apiPort;
    }

    @Override
    public UserApi setPort(String port){
        this.port = port;
        return this;
    }

    @Override
    public UserApi setPort(Integer port){
        return setPort(Integer.toString(port));
    }

    @Override
    public UserApi setCredentials(String loginUsername, String password){
        api.setCredentials(loginUsername, password);
        return this;
    }

    public UserApi setSessionId(String sessionId){
        api.setSessionId(sessionId);
        return this;
    }

    @Override
    public ResponseEntity<Usuario> findByUsername(String username) throws HttpClientErrorException {
        return api.getRestClient().get().uri(findByUsername, port, username)
                .retrieve().toEntity(Usuario.class);
    }

    @Override
    public ResponseEntity<Usuario> update(Usuario user) throws HttpClientErrorException{
        return api.getRestClient().put().uri(updateUsuario, port).body(user)
                .retrieve().toEntity(Usuario.class);
    }

    @Override
    public ResponseEntity<UsuarioList> findAll() throws HttpClientErrorException {
        return api.getRestClient().get().uri(findAllUsuarios, port)
                .retrieve().toEntity(UsuarioList.class);
    }

    @Override
    public ResponseEntity<GroupList> authenticateUser(Usuario user) throws HttpClientErrorException{
        return api.getRestClient().post().uri(authUserAddress, port)
                .retrieve().toEntity(GroupList.class);
    }

    @Override
    public ResponseEntity<String> emailValidation(String token) throws HttpClientErrorException {
        log.trace("USER_API::EmailValidation $token: {}", token);
        return api.getRestClient().get().uri(usuarioEmailValidation, port, token)
                .retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<Usuario> create(Usuario usuario) throws HttpClientErrorException {
        log.trace("USER_API::Create $uri: {}, $port: {}", createUsuario, port);
        return api.getRestClient().post().uri(createUsuario, port)
                .contentType(APPLICATION_JSON)
                .body(usuario).retrieve().toEntity(Usuario.class);
    }

    @Override
    public ResponseEntity<String> delete(String username) throws HttpClientErrorException {
        return api.getRestClient().delete().uri(deleteUsuario, port, username)
                .retrieve().toEntity(String.class);
    }
}
