package com.laetienda.messenger.controller;

import com.laetienda.model.messager.EmailMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/v0/email/preview")
public class PreviewController {
    final private static Logger log = LoggerFactory.getLogger(PreviewController.class);

    @PostMapping("/hola.html")
    public String hola(){
        log.trace("Running /api/v0/email/preview/hola.html controller ");
        return "default/test.html";
    }

    @PostMapping("/template.html")
    public String getTemplate(@Valid @RequestBody EmailMessage message, HttpServletRequest req){
        log.trace("Running email preview");

        req.setAttribute("message", message);
        log.trace("$path: {}", message.getView());

        return message.getView();
    }
}
