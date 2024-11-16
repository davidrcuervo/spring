package com.laetienda.utils.service.api;

import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.lib.service.ToolBoxServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

public class ApiClientImplementation implements ApiClient {
    private final static Logger log = LoggerFactory.getLogger(ApiClientImplementation.class);

    private String loginUsername;
    private String password;
    private String sessionId;
    private ToolBoxService tb;
    private String port;

    public ApiClientImplementation(){
        this.tb = new ToolBoxServiceImpl();
        loginUsername = password = sessionId = null;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public RestClient getRestClient(){

        RestClient result = null;
        if(password != null && loginUsername != null){
            result = RestClient.builder()
                    .defaultHeader(HttpHeaders.AUTHORIZATION, tb.getEncode64(loginUsername, password))
                    .build();
            password = null;
            loginUsername = null;

        }else if(sessionId != null && !sessionId.isBlank()){
            result = RestClient.builder()
//                    .defaultHeader(HttpHeaders.COOKIE, Base64.getEncoder().encodeToString(sessionId.getBytes()))
                    .defaultHeader(HttpHeaders.COOKIE, sessionId)
                    .build();
//            sessionId = null;
        }else{
            result = RestClient.builder().build();
        }

        return  result;
    }

    @Override
    public ApiClient setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    @Override
    public ApiClient setCredentials(String loginUsername, String password) {
        this.loginUsername = loginUsername;
        this.password = password;
        return this;
    }

    @Override
    public ApiClient setPort(String port) {
        this.port = port;
        return this;
    }

    @Override
    public ApiClient setPort(Integer port) {
        setPort(Integer.toString(port));
        return this;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public String getUsername() {
        return loginUsername;
    }

    @Override
    public String getSession() {
        return this.sessionId;
    }

    @Override
    public ResponseEntity<String> startSession(String loginAddress, String logoutAddress) throws HttpClientErrorException {
        log.trace("API::StartSession $loginAddress: {}", loginAddress);

        if(sessionId != null){
            endSession(logoutAddress);
        }

        ResponseEntity<String> loginResp = getRestClient().post()
                .uri(loginAddress, getPort())
                .retrieve().toEntity(String.class);
        String session = loginResp.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];
        log.trace("API::startSession $session: {}", session);
        setSessionId(session);

        return loginResp;
    }

    @Override
    public ResponseEntity<String> endSession(String logoutAddress) throws HttpClientErrorException {
        log.trace("API::endSession $logoutAddress: {}", logoutAddress);

        ResponseEntity<String> resp = getRestClient().post()
                .uri(logoutAddress, getPort())
                .retrieve().toEntity(String.class);

        if(sessionId == null){
            log.trace("API::endSession. sessionId is null. session has not been set");
        }else{
            setSessionId(null);
        }
        return resp;
    }
}
