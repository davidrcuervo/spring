package com.laetienda.schema.controller;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;
import com.laetienda.schema.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("${api.schema.root}")
public class SchemaController {
    private final static Logger log = LoggerFactory.getLogger(SchemaController.class);

    @Autowired private ItemService itemService;

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

    @PostMapping("${api.schema.create}")
    public ResponseEntity<DbItem> create(@RequestBody DbItem item) throws NotValidCustomException{
        log.debug("SCHEMA_CONTROLLER::create");
        return ResponseEntity.ok(itemService.create(item));
    }
}