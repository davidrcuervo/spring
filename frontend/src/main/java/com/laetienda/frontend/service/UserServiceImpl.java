package com.laetienda.frontend.service;

import com.laetienda.model.user.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class UserServiceImpl implements UserService{
    final private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private RestTemplate restclient;

    @Value("${api.user.add}")
    private String apiAddUrl;

    @Override
    public Usuario find(String username) {
        return null;
    }

    @Override
    public Usuario post(Usuario user) {
        Usuario result = null;
        log.debug("$api.user.add: {}", apiAddUrl);

        try {
            ResponseEntity<Usuario> response = restclient.postForEntity(apiAddUrl, user, Usuario.class);

            if(response.getStatusCode() == HttpStatus.OK){
                result = response.getBody();
            }else{
                log.debug("response code: {}", response.getStatusCode());
            }

        }catch (RestClientException e){
            log.debug(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Usuario delete(Usuario user) {
        return null;
    }

    @Override
    public Usuario modify(Usuario user) {
        return null;
    }
}
