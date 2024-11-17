package com.laetienda.utils.service.api;

import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class UserApiImplementation extends ApiClientImplementation implements UserApi {
    final private static Logger log = LoggerFactory.getLogger(UserApiImplementation.class);

    @Autowired
    private Environment env;

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

//    @Autowired
//    private ApiClientService api;

//    private String port;

    public UserApiImplementation() {
        log.trace("USERAPI::Constructor. $port: {}", apiPort);
        setPort(apiPort);
    }

//    @Override
//    public UserApi setPort(String port){
//        this.port = port;
//        return this;
//    }

//    @Override
//    public UserApi setPort(Integer port){
//        return setPort(Integer.toString(port));
//    }

//    @Override
//    public UserApi setCredentials(String loginUsername, String password){
//        api.setCredentials(loginUsername, password);
//        return this;
//    }

//    public UserApi setSessionId(String sessionId){
//        api.setSessionId(sessionId);
//        return this;
//    }

    @Override
    public ResponseEntity<Usuario> findByUsername(String username) throws HttpClientErrorException {
        return getRestClient().get().uri(findByUsername, getPort(), username)
                .retrieve().toEntity(Usuario.class);
    }

    @Override
    public ResponseEntity<Usuario> update(Usuario user) throws HttpClientErrorException{
        return getRestClient().put().uri(updateUsuario, getPort()).body(user)
                .retrieve().toEntity(Usuario.class);
    }

    @Override
    public ResponseEntity<UsuarioList> findAll() throws HttpClientErrorException {
        return getRestClient().get().uri(findAllUsuarios, getPort())
                .retrieve().toEntity(UsuarioList.class);
    }

    @Override
    public ResponseEntity<GroupList> authenticateUser(Usuario user) throws HttpClientErrorException{
        return getRestClient().post().uri(authUserAddress, getPort())
                .retrieve().toEntity(GroupList.class);
    }

    @Override
    public ResponseEntity<String> login() throws HttpClientErrorException {
        String address = String.format("%s/%s", env.getProperty("api.usuario"), env.getProperty("api.usuario.login"));
        log.trace("USER_API::login. $address: {}", address);
        return getRestClient().post()
                .uri(address, getPort())
                .retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<String> logout() throws HttpClientErrorException {
        String address = String.format("%s/logout", env.getProperty("api.usuario.url"));
        log.trace("USER_API::logout. $address: {}", address);
        ResponseEntity<String> resp = getRestClient().post()
                .uri(address, getPort())
                .retrieve().toEntity(String.class);
        setSessionId(null);
        return resp;
    }

    @Override
    public ResponseEntity<String> emailValidation(String token) throws HttpClientErrorException {
        log.trace("USER_API::EmailValidation $token: {}", token);
        return getRestClient().get().uri(usuarioEmailValidation, getPort(), token)
                .retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<String> startSession() throws HttpClientErrorException {
        log.trace("USER_API::startSession $userame: {}", getUsername());
        String loginAddress = String.format("%s/%s", env.getProperty("api.usuario"), env.getProperty("api.usuario.login"));
        String logoutAddress = env.getProperty("api.usuario.logout");
//        ResponseEntity<String> resp = login();
//        String session = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];
//        log.trace("USER_API::startSession $session: {}", session);
//        super.setSessionId(session);

        return super.startSession(loginAddress, logoutAddress);
    }

    @Override
    public ResponseEntity<String> endSession() throws HttpClientErrorException {
        log.trace("USER_API::endSession $session: {}", getSession());
//        ResponseEntity<String> resp = logout();
//        setSessionId(null);

        String logoutAddress = env.getProperty("api.usuario.logout");

        return super.endSession(logoutAddress);
    }

    @Override
    public ResponseEntity<Usuario> create(Usuario usuario) throws HttpClientErrorException {
        log.trace("USER_API::Create $uri: {}, $port: {}", createUsuario, getPort());
        return getRestClient().post().uri(createUsuario, getPort())
                .contentType(APPLICATION_JSON)
                .body(usuario).retrieve().toEntity(Usuario.class);
    }

    @Override
    public ResponseEntity<String> delete(String username) throws HttpClientErrorException {
        return getRestClient().delete().uri(deleteUsuario, getPort(), username)
                .retrieve().toEntity(String.class);
    }

    public String getPort(){

        if(super.getPort() == null){
            super.setPort(apiPort);
        }

        log.trace("USER_API::getPort. $port: {}", super.getPort());
        return super.getPort();
    }
}
