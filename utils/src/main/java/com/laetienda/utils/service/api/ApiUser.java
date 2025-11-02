package com.laetienda.utils.service.api;

import com.laetienda.lib.exception.NotValidCustomException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

public interface ApiUser {
    String isUsernameValid(String username) throws NotValidCustomException;
    String isUserIdValid(String userId) throws NotValidCustomException;
    String getToken(String username, String password) throws  NotValidCustomException;
    String getCurrentUserId() throws NotValidCustomException;
}
