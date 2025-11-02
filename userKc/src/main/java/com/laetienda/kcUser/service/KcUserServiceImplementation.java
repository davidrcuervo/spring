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
    public String isUsernameValid(String username) throws NotValidCustomException {
        log.debug("USER_SERVICE::isUsernameValid. $username: {}", username);
        return findByUsername(username).getId();
    }

    @Override
    public String isUserIdValid(String userId) throws NotValidCustomException {
        log.debug("USER_SERVICE::isUserIdValid. $userId: {}", userId);
        KcUser result = repo.findByUserId(userId);

        if(isUserValid(result)){
            log.trace("USER_SERVICE::isUserIdValid: TRUE. $userId: {}", result.getId());
            return result.getId();
        } else{
            return null;
        }
    }

    private KcUser findByUsername(String username) throws NotValidCustomException{
        log.trace("USER_SERVICE::findByUsername. $username: {}", username);
        List<KcUser> result = repo.findByUsername(username);

        if (result == null || result.isEmpty()) {
            String message = String.format("User, %s, does not exist.", username);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "username");
        }

        if (result.size() > 1){
            String message = String.format("Username, %s, exists more than once", "username");
            log.error(message);
            throw new NotValidCustomException(message, HttpStatus.CONFLICT, "username");
        }

        isUserValid(result.getFirst());

        return result.getFirst();
    }

    private boolean isUserValid(KcUser user) throws NotValidCustomException{
        log.trace("USER_SERVICE::isUserValid");
        boolean result = true;

        if(user == null){
            String message = "Username or user id does not exist";
            log.info(message);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "user");
        }

        if(!user.isEmailVerified() || user.getEmail() == null || user.getEmail().isBlank()){
            String message = String.format("Username, %s, does not have a verified email address", user.getUsername());
            log.error(message);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "username");
        }

        return result;
    }
}
