package com.laetienda.messenger.controller;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.messenger.service.EmailService;
import com.laetienda.model.messager.EmailMessage;
import com.laetienda.model.user.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v0/email")
public class EmailController {
    final private static Logger log = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService service;

    @GetMapping("${api.messenger.helloworld}")
    public ResponseEntity<String> helloWorld(){
        log.trace("MESSENGER_CONTROLLER::helloWorld");
        return ResponseEntity.ok("Hola mundo");
    }

    @GetMapping("${api.messenger.test}")
    public ResponseEntity<Boolean> testMailer(){
        log.trace("MESSENGER_CONTROLLER::testMailer");
        service.testMailer();
        return ResponseEntity.ok(true);
    }

    @PostMapping("${api.messenger.testSimplePost}")
    public ResponseEntity<Boolean> testSimplePost(@RequestBody Usuario usuario) throws NotValidCustomException{
        log.trace("MESSENGER_CONTROLLER::testSimplePost. $email: {}", usuario.getEmail());
        throw new NotValidCustomException("test exception", HttpStatus.BAD_REQUEST);
//        return ResponseEntity.badRequest(false);
    }

    @PostMapping("${api.messenger.send}")
    public ResponseEntity<Boolean> sendMessage(@Valid @RequestBody EmailMessage message) throws NotValidCustomException {
        log.trace("MESSENGER_CONTROLLER::sendMessage.");
        service.sendMessage(message);
        return ResponseEntity.ok(true);
    }
}