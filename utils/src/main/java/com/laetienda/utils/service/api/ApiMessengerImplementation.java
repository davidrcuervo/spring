package com.laetienda.utils.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.model.messager.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

@Component
public class ApiMessengerImplementation implements ApiMessenger{
    final static private Logger log = LoggerFactory.getLogger(ApiMessengerImplementation.class);

    @Autowired private Environment env;
    @Autowired private ObjectMapper json;

    private final RestClient client;

    public ApiMessengerImplementation(RestClient restClient){
        this.client = restClient;
    }

    @Override
    public void send(EmailMessage message) throws HttpStatusCodeException {
        String address = env.getProperty("api.mail.uri.send", "send");
        log.debug("API_EMAIL::send. $address: {}", address);

        try {
            beforeSend(message);

            ResponseEntity<String> response = client.post().uri(address)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.writeValueAsBytes(message)).retrieve().toEntity(String.class);
        } catch (JsonProcessingException e) {
            log.error("API_MAIL::send. $error: {}", e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void beforeSend(EmailMessage message) throws HttpStatusCodeException {

        try {

            if(message.getDbItem() != null) {

                if (message.getClazzName() == null || message.getClazzName().isBlank()) {
                    String msj = "EmailMessage has not DbItem but class name is not specified.";
                    log.error("API_MAIL::beforeSend.. $error: {}", msj);
                    throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, msj);
                }

                String temp = json.writeValueAsString(message.getDbItem());
                message.setJsonItem(temp);
            }

        } catch (JsonProcessingException e) {
            log.error("API_MAIL::beforeSend. $error: {}", e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
