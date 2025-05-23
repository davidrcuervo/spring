package com.laetienda.kcUser.service;

import com.laetienda.kcUser.respository.KcUserRepository;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.kc.KcUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;
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

    @Override
    public String isValidUser(String username) throws NotValidCustomException {
        log.debug("USER_SERVICE::isValidUser. $username: {}", username);
        List<KcUser> result = repo.findByUsername(username);

        if (result == null || result.isEmpty()) {
            String message = String.format("User, %s, does not exist.", username);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "Username");

        } else if (result.size() > 1){
            String message = String.format("Username, %s, exists more than once", username);
            log.error(message);
            throw new NotValidCustomException(message, HttpStatus.CONFLICT, username);
        }

        return result.getFirst().getId();
    }
}
