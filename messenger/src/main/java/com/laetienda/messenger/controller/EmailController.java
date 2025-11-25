package com.laetienda.messenger.controller;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.messenger.service.EmailService;
import com.laetienda.model.messager.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.client.HttpStatusCodeException;

@RestController
@RequestMapping("${api.mail.folder}")
public class EmailController {
    final private static Logger log = LoggerFactory.getLogger(EmailController.class);

    @Autowired private EmailService service;

    @GetMapping("${api.mail.file.test}")
    public ResponseEntity<Void> test() throws HttpStatusCodeException {
        log.info("MAIL_CONTROLLER::test.");
        service.test();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("${api.mail.file.send}")
    public ResponseEntity<Void> send(@Valid @RequestBody EmailMessage data) throws HttpStatusCodeException {
        log.info("MAIL_CONTROLLER::send.");
        service.send(data);
        return ResponseEntity.noContent().build();
    }
}