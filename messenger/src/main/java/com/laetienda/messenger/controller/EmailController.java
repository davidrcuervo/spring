package com.laetienda.messenger.controller;

import com.laetienda.messenger.service.EmailService;
import com.laetienda.model.messager.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v0/email")
public class EmailController {
    final private static Logger log = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService service;

    @GetMapping("hola.html")
    public ResponseEntity<String> hola(){
        log.trace("running hola email controller");
        return ResponseEntity.ok("Hola mundo");
    }


    @PostMapping("sendMessage.html")
    public ResponseEntity<Boolean> sendMessage(@Valid @RequestBody EmailMessage message) throws ResponseStatusException {
        log.trace("Running send message email controller");
        service.sendMessage(message);
        return ResponseEntity.ok(true);
    }
}
