package com.laetienda.utils.service.api;

import com.laetienda.model.messager.EmailMessage;
import org.springframework.web.client.HttpStatusCodeException;

public interface ApiMessenger {
    void send(EmailMessage message) throws HttpStatusCodeException;
}
