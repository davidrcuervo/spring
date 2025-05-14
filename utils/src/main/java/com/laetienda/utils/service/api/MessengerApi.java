package com.laetienda.utils.service.api;

import com.laetienda.model.messager.EmailMessage;
import com.laetienda.model.user.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

public interface MessengerApi extends ApiClient {
    ResponseEntity<String> helloWord() throws HttpClientErrorException;
    ResponseEntity<String> testMailer() throws HttpClientErrorException;
    ResponseEntity<String> sendMessage(EmailMessage message) throws HttpClientErrorException;
    ResponseEntity<String> testSimplePost(Usuario user) throws HttpClientErrorException;
}
