package com.laetienda.frontend.controller.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

@ControllerAdvice
public class ExceptionController {
    private final static Logger log = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex){
        log.info("EXCEPTION_CONTROLLER::handleException | {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<String> handleHttpStatusCodeException(HttpStatusCodeException ex){
        log.info("EXCEPTION_CONTROLLER::handleHttpStatusCodeException | {}", ex.getMessage());
        return  ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }
}
