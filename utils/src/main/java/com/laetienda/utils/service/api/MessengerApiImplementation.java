package com.laetienda.utils.service.api;

import com.laetienda.model.messager.EmailMessage;
import com.laetienda.model.user.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

public class MessengerApiImplementation extends ApiClientImplementation implements MessengerApi{
    private static final Logger log = LoggerFactory.getLogger(MessengerApiImplementation.class);

    @Value("${api.messenger.port}")
    private String port;

    @Value("${api.messenger}/${api.messenger.helloworld}")
    private String helloWorld;

    @Value("${api.messenger}/${api.messenger.test}")
    private String testMailer;

    @Value("${api.messenger}/${api.messenger.send}")
    private String send;

    @Value("${api.messenger}/${api.messenger.testSimplePost}")
    private String testSimplePost;

    @Override
    public ResponseEntity<String> helloWord() throws HttpClientErrorException {
        return getRestClient().get()
                .uri(helloWorld, getPort())
                .retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<String> testMailer() throws HttpClientErrorException {
        return getRestClient().get()
                .uri(testMailer, getPort())
                .retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<String> sendMessage(EmailMessage message) throws HttpClientErrorException {
        log.trace("MESSENGER_API::sendMessage. $uri: {}, $port: {}", send, getPort());
        return getRestClient().post()
                .uri(send, getPort())
                .body(message)
                .retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<String> testSimplePost(Usuario user) throws HttpClientErrorException {
        return getRestClient().post()
                .uri(testSimplePost, getPort())
                .body(user)
                .retrieve().toEntity(String.class);
    }

    public String getPort(){
        if(super.getPort() == null){
            log.trace("MESSENGER_API::setPort. $port: {}", port);
            super.setPort(port);
        }

        return super.getPort();
    }
}
