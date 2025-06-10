package com.laetienda.frontend.repository;

import com.laetienda.model.kc.KcUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

@Repository
public class UserRepositoryImplementation implements UserRepository{
    final static private Logger log = LoggerFactory.getLogger(UserRepositoryImplementation.class);

    final private RestClient client;

    @Autowired private Environment env;

    UserRepositoryImplementation(RestClient restClient){
        this.client = restClient;
    }

    @Override
    public KcUser getUserAccount() {
        String address = env.getProperty("api.kcUser.find.uri" ,"/home.html");
        log.debug("USER_REPOSITORY::getUserAccount. $address: {}", address);

        return client.get().uri(address)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(KcUser.class).getBody();
    }
}
