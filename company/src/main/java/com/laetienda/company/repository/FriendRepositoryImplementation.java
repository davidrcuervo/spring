package com.laetienda.company.repository;

import com.laetienda.utils.service.api.ApiSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FriendRepositoryImplementation implements FriendRepository {
    private final static Logger log = LoggerFactory.getLogger(FriendRepositoryImplementation.class);

    @Autowired ApiSchema apiSchema;
}
