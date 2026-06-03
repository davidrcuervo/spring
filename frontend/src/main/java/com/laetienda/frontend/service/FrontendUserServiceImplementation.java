package com.laetienda.frontend.service;

import com.laetienda.model.kc.KcUser;
import com.laetienda.utils.service.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("usr")
public class FrontendUserServiceImplementation implements FrontendUserService {
    final static private Logger log = LoggerFactory.getLogger(FrontendUserServiceImplementation.class);

    private final ApiUser api;

    public FrontendUserServiceImplementation(ApiUser apiUser){
        this.api = apiUser;
    }

    @Override
    public KcUser getCurrentUser() {
        log.debug("USER_SERVICE::getCurrentUser.");
        return api.getCurrentUser();
    }

    @Override
    public KcUser getUser(String userId){
        log.debug("USER_SERVICE::getUser. | $userId: {}", userId);
        return api.getUserWithWebAppService(userId);
    }
}
