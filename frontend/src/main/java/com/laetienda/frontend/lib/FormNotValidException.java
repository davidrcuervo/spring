package com.laetienda.frontend.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.model.Mistake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Date;

public class FormNotValidException extends HttpClientErrorException {
    final private static Logger log = LoggerFactory.getLogger(FormNotValidException.class);
    private Mistake mistake;
    public FormNotValidException(Mistake mistake, HttpClientErrorException e){
        super (e.getMessage(), e.getStatusCode(), e.getStatusText(), e.getResponseHeaders(), e.getResponseBodyAsByteArray(), null);
        setMistake(mistake);
    }

    public FormNotValidException(HttpClientErrorException e){
        super (e.getMessage(), e.getStatusCode(), e.getStatusText(), e.getResponseHeaders(), e.getResponseBodyAsByteArray(), null);

        try {
            mistake = new ObjectMapper().readValue(e.getResponseBodyAsString(), Mistake.class);
        } catch (JsonProcessingException ex) {
            log.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
            log.debug("Exception.", ex);

            mistake = new Mistake();
            mistake.setTimestamp(new Date());
            mistake.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            mistake.add("form", "Internal error while getting errors");
        }
    }

    public FormNotValidException(Exception ex){
        super(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        log.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        log.debug("Exception.", ex);

        mistake = new Mistake();
        mistake.setTimestamp(new Date());
        mistake.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        mistake.add("form", "Internal error while getting errors");
    }

    public Mistake getMistake() {
        return mistake;
    }

    public void setMistake(Mistake mistake) {
        this.mistake = mistake;
    }
}
