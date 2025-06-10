package com.laetienda.utils.service.api;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

public interface ApiUser {
    String isUsernameValid(String username) throws HttpClientErrorException, HttpServerErrorException;
    String isUserIdValid(String userId);
}
