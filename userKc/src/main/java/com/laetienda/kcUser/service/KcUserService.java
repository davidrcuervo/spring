package com.laetienda.kcUser.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.kc.KcUser;
import org.springframework.util.MultiValueMap;

public interface KcUserService {
    KcUser find();
    String getToken(MultiValueMap<String, String> creds);
    String isUsernameValid(String username) throws NotValidCustomException;
    String isUserIdValid(String userId) throws NotValidCustomException;
}
