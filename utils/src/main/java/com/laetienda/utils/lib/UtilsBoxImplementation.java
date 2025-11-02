package com.laetienda.utils.lib;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.utils.service.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UtilsBoxImplementation implements UtilsBox{
    private final static Logger log = LoggerFactory.getLogger(UtilsBoxImplementation.class);

    private ApiUser apiUser;

    @Override
    public String getCurrentUser() throws NotValidCustomException{
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.trace("UTILS_BOX::getCurrentUser. $loggedUser: {}", userId);

        apiUser.isUserIdValid(userId);

        return userId;
    }
}
