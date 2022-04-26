package com.laetienda.usuario.controller;

import com.laetienda.lib.model.Mistake;
import com.laetienda.usuario.repository.MistakeRepoImpl;
import com.laetienda.usuario.repository.MistakeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionController extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ExceptionController.class);

    @Override
    public ResponseEntity handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request
    ){
        MistakeRepository mistake = new MistakeRepoImpl(status.value());

        for(FieldError error : ex.getBindingResult().getFieldErrors()){
            log.debug("{}: {}", error.getField(), error.getDefaultMessage());
            mistake.addMistake(error.getField(), error.getDefaultMessage());
        }

        return new ResponseEntity<>(mistake.get(), headers, status);
    }
}
