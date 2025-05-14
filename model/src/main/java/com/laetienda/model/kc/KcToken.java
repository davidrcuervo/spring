package com.laetienda.model.kc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KcToken {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String type;

    @JsonProperty("session_state")
    private String state;

    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
