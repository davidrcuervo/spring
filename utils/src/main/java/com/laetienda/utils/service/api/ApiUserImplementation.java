package com.laetienda.utils.service.api;

import com.laetienda.lib.exception.CustomRestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Component
public class ApiUserImplementation implements ApiUser{
    private final static Logger log = LoggerFactory.getLogger(ApiUserImplementation.class);
    private final RestClient client;

    @Autowired Environment env;
    @Value("${kc.client-id}") String webappClientId;

    public ApiUserImplementation(RestClient restClient){
        this.client = restClient;
    }

    @Override
    public String isUsernameValid(String username) throws HttpClientErrorException, HttpServerErrorException {
        String address = env.getProperty("api.kcUser.isUsernameValid.uri", "#");
        log.debug("API_USER::isValidUser. $username: {} | $address: {}", username, address);
        return client.get().uri(address, username)
                .attributes(clientRegistrationId(webappClientId))
                .retrieve().toEntity(String.class).getBody();
    }

    @Override
    public String isUserIdValid(String userId) throws CustomRestClientException{
        String address = env.getProperty("api.kcUser.isUserIdValid.uri", "#");
        log.debug("API_USER::isUserIdValid. $userId: {} | $address: {}", userId, address);

        try{
            return client.get().uri(address, userId)
                    .attributes(clientRegistrationId(webappClientId))
                    .retrieve().toEntity(String.class).getBody();
        }catch(HttpClientErrorException e){
            throw new CustomRestClientException(e);
        }
    }
}
