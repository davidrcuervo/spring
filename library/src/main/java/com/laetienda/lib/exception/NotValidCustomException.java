package com.laetienda.lib.exception;

import com.laetienda.lib.model.Mistake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class NotValidCustomException extends Exception{
    final private static Logger log = LoggerFactory.getLogger(NotValidCustomException.class);
    private Mistake mistake;
    private HttpStatus status;

    public NotValidCustomException(String message, HttpStatus statuscode){
        super(message);
        this.status = statuscode;
        mistake = new Mistake(statuscode.value());
    }

    /**
     *
     * @param message error message
     * @param statuscode HttpStatus.code
     * @param key name of the pointer error. Used by forms
     */
    public NotValidCustomException(String message, HttpStatus statuscode, String key){
        super(message);
        this.status = statuscode;
        mistake = new Mistake(statuscode.value());
        addError(key, message);
    }

    public NotValidCustomException(String message, HttpStatus statuscode, String pointer, String detail){
        super(message);
        this.status = statuscode;
        mistake = new Mistake(statuscode.value());
        addError(pointer, detail);
    }

    public void addError(String key, String message){
        log.debug("Error: ${} -> {}", key, message);
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
