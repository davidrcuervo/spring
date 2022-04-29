package com.laetienda.lib.exception;

import com.laetienda.lib.model.Mistake;
import org.springframework.http.HttpStatus;

public class NotValidCustomException extends Exception{

    private Mistake mistake;
    private HttpStatus status;

    public NotValidCustomException(String message, HttpStatus statuscode){
        super(message);
        this.status = statuscode;
        mistake = new Mistake(statuscode.value());

    }

    public void addError(String key, String message){
        mistake.add(key, message);
    }

    public Mistake getMistake(){
        return mistake;
    }

    public void setMistake(Mistake mistake) {
        this.mistake = mistake;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
