package com.laetienda.kcUser.service;

import com.laetienda.kcUser.respository.KcUserRepository;
import com.laetienda.model.kc.KcUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
public class KcUserServiceImplementation implements KcUserService{
    final static private Logger log = LoggerFactory.getLogger(KcUserServiceImplementation.class);

    @Autowired private KcUserRepository repo;

    @Override
    public KcUser find() {
        log.debug("USER_SERVICE::find");
        return repo.find();
    }

    @Override
    public String getToken(MultiValueMap<String, String> creds) {
        log.debug("USER_SERVICE::getToken. $username: {}", creds.get("username"));
        return repo.getToken(creds).getAccessToken();
    }
}
