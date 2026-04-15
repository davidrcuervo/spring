package com.laetienda.utils.service.api;

public interface ApiRestClient {
    String getJwtToken();
    void setJwtToken(String jwtToken);
    String getClientRegistrationId();
    void setClientRegistrationId(String clientRegistrationId);
}
