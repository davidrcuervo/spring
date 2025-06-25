package com.laetienda.lib.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.model.Mistake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Date;

@Deprecated
public class CustomRestClientExceptionDeprecated extends HttpClientErrorException {
    final private static Logger log = LoggerFactory.getLogger(CustomRestClientExceptionDeprecated.class);
    private Mistake mistake;
    public CustomRestClientExceptionDeprecated(Mistake mistake, HttpClientErrorException e){
        super (e.getMessage(), e.getStatusCode(), e.getStatusText(), e.getResponseHeaders(), e.getResponseBodyAsByteArray(), null);
        setMistake(mistake);
    }

    public CustomRestClientExceptionDeprecated(HttpClientErrorException e){
        super (e.getMessage(), e.getStatusCode(), e.getStatusText(), e.getResponseHeaders(), e.getResponseBodyAsByteArray(), null);

        try {
            mistake = new ObjectMapper().readValue(e.getResponseBodyAsString(), Mistake.class);
        } catch (JsonProcessingException ex) {
            log.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
            log.debug(ex.getMessage(), ex);

            mistake = new Mistake();
            mistake.setTimestamp(new Date());
            mistake.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            mistake.add("form", "Internal error while getting errors");
        }
    }

    public CustomRestClientExceptionDeprecated(Exception ex){
        super(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        log.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        log.debug(ex.getMessage(), ex);

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
