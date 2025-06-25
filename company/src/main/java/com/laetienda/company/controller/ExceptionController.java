package com.laetienda.company.controller;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.model.Mistake;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(NotValidCustomException.class)
    public ResponseEntity<Mistake> handleNotValiCustomException(NotValidCustomException ex){
        return new ResponseEntity<Mistake>(ex.getMistake(), ex.getStatus());
    }
}
