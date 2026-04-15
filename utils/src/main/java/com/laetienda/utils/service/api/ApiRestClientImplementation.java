package com.laetienda.utils.service.api;

public abstract class ApiRestClientImplementation implements ApiRestClient {

    private String jwtToken;
    private String clientRegistrationId;

    @Override
    public String getJwtToken() {
        return jwtToken;
    }

    @Override
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public String getClientRegistrationId() {
        return clientRegistrationId;
    }

    @Override
    public void setClientRegistrationId(String clientRegistrationId) {
        this.clientRegistrationId = clientRegistrationId;
    }
}
