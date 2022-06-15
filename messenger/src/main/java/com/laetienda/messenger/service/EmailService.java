package com.laetienda.messenger.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.messager.EmailMessage;
import org.springframework.web.server.ResponseStatusException;

public interface EmailService {

    /**
     * Send message
     * @param message
     * @throws NotValidCustomException
     */
    void sendMessage(EmailMessage message) throws ResponseStatusException;
}
