package com.laetienda.utils.service.api;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

public interface ApiUser {
    String isValidUser(String username) throws HttpClientErrorException, HttpServerErrorException;
}
