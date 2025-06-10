package com.laetienda.frontend.service;

import com.laetienda.frontend.repository.UserRepository;
import com.laetienda.model.kc.KcUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplementation implements UserService{
    final static private Logger log = LoggerFactory.getLogger(UserServiceImplementation.class);

    @Autowired UserRepository repo;

    @Override
    public KcUser getUserAccount() {
        log.debug("USER_SERVICE::getUserAccount.");
        return repo.getUserAccount();
    }
}
