package com.laetienda.frontend.lib;

import com.laetienda.lib.model.Mistake;
import org.springframework.web.client.HttpClientErrorException;

public class FormNotValidException extends HttpClientErrorException {

    private Mistake mistake;
    public FormNotValidException(Mistake mistake, HttpClientErrorException e){
        super (e.getMessage(), e.getStatusCode(), e.getStatusText(), e.getResponseHeaders(), e.getResponseBodyAsByteArray(), null);
        setMistake(mistake);
    }

    public Mistake getMistake() {
        return mistake;
    }

    public void setMistake(Mistake mistake) {
        this.mistake = mistake;
    }
}
