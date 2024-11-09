package com.laetienda.schema.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("${api.schema.root}")
public class SchemaController {
    private final static Logger log = LoggerFactory.getLogger(SchemaController.class);

    @PostMapping("${api.schema.helloAll}")
    public ResponseEntity<String> helloAll(){
        log.debug("SCHEMA_CONTROLLER::helloAll");
        return ResponseEntity.ok("Hello World!!");
    }

    @PostMapping("${api.schema.helloUser}")
    public ResponseEntity<String> helloUser(Principal principal){
        log.debug("SCHEMA_CONTROLLER::helloUser $username: {}", principal.getName());
        return ResponseEntity.ok("Hello " + principal.getName());
    }

    @PostMapping("${api.schema.helloValidatedUser}")
    public ResponseEntity<String> helloValidatedUser(Principal principal){
        log.debug("SCHEMA_CONTROLLER::helloValidateUser: {}", principal.getName());
        return ResponseEntity.ok("Hello " + principal.getName());
    }
}
