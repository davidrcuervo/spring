package com.laetienda.kcUser.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.kc.KcUser;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public interface KcUserService {
    KcUser find();
    String getToken(MultiValueMap<String, String> creds);
    String isValidUser(String username) throws NotValidCustomException;
}
