package com.laetienda.kcUser.respository;

import com.laetienda.model.kc.KcToken;
import com.laetienda.model.kc.KcUser;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public interface KcUserRepository {
    KcUser find();
    KcToken getToken(MultiValueMap<String, String> creds);
    List<KcUser> findByUsername(String username);
}
