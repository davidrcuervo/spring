package com.laetienda.utils.service;

import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.lib.service.ToolBoxServiceImpl;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Base64;

public class UserAndGroupApiRepoImpl implements UserAndGroupApiRepository{
    final private static Logger log = LoggerFactory.getLogger(UserAndGroupApiRepoImpl.class);

    private ToolBoxService tb;

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
    private String port;

    private String loginUsername;
    private String password;
    private String sessionId;

    public UserAndGroupApiRepoImpl() throws IOException {
        this.port = apiPort;
        tb = new ToolBoxServiceImpl();
        loginUsername = null;
        password = null;
        sessionId = null;
    }

    @Override
    public UserAndGroupApiRepository setPort(Integer port) {
        this.port = Integer.toString(port);
        return this;
    }

    @Override
    public UserAndGroupApiRepository setPort(String port) {
        this.port = port;
        return this;
    }

    @Override
    public UserAndGroupApiRepository setCredentials(String loginUsername, String password){
        this.loginUsername = loginUsername;
        this.password = password;
        return this;
    }

    @Override
    public UserAndGroupApiRepository setSessionId(String sessionId){
        this.sessionId = sessionId;
        return this;
    }

    private RestClient getRestClient(){

        RestClient result = null;
        if(password != null && loginUsername != null){
            result = RestClient.builder()
                    .defaultHeader(HttpHeaders.AUTHORIZATION, tb.getEncode64(loginUsername, password))
                    .build();
            password = null;
            loginUsername = null;

        }else if(sessionId != null && !sessionId.isBlank()){
            result = RestClient.builder()
                    .defaultHeader(HttpHeaders.COOKIE, Base64.getEncoder().encodeToString(sessionId.getBytes()))
                    .build();
            sessionId = null;
        }else{
            result = RestClient.builder().build();
        }
        return  result;
    }

    @Override
    public ResponseEntity<Usuario> findByUsername(String username) throws HttpClientErrorException {
        return getRestClient().get().uri(findByUsername, port, username)
                .retrieve().toEntity(Usuario.class);
    }

    @Override
    public ResponseEntity<Usuario> update(Usuario user) throws HttpClientErrorException{
        return getRestClient().put().uri(updateUsuario, port).body(user)
                .retrieve().toEntity(Usuario.class);
    }

    @Override
    public ResponseEntity<UsuarioList> findAll() throws HttpClientErrorException {
        return getRestClient().get().uri(findAllUsuarios, port)
                .retrieve().toEntity(UsuarioList.class);
    }

    @Override
    public ResponseEntity<GroupList> authenticateUser(Usuario user) throws HttpClientErrorException{
        return getRestClient().post().uri(authUserAddress, port)
                .retrieve().toEntity(GroupList.class);
    }

    @Override
    public ResponseEntity<Usuario> create(Usuario usuario) throws HttpClientErrorException {
        return getRestClient().post().uri(createUsuario, port)
                .body(usuario).retrieve().toEntity(Usuario.class);
    }

    @Override
    public ResponseEntity<String> delete(String username) throws HttpClientErrorException {
        return getRestClient().delete().uri(deleteUsuario, port, username)
                .retrieve().toEntity(String.class);
    }
}
