package com.laetienda.messenger.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.messager.EmailMessage;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

public interface EmailService {

    void send(EmailMessage message) throws HttpStatusCodeException;
    void test() throws HttpStatusCodeException;
}
