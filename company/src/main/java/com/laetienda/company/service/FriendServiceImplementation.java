package com.laetienda.company.service;

import com.laetienda.company.repository.FriendRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FriendServiceImplementation implements FriendService{
    private final static Logger log = LoggerFactory.getLogger(FriendServiceImplementation.class);

    @Autowired private FriendRepository repo;
}
