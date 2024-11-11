package com.laetienda.utils.service.api;

import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.lib.service.ToolBoxServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

public class ApiClientImplementation implements ApiClient {

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
}
