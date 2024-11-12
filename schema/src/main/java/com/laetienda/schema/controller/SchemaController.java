package com.laetienda.schema.controller;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.model.schema.DbItem;
import com.laetienda.schema.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.Base64;
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

    @PostMapping("${api.schema.createPath}")
    public ResponseEntity<String> create(@RequestParam(required = true) String clazz, @RequestBody String data) throws NotValidCustomException{
        String clazzName = new String(Base64.getUrlDecoder().decode(clazz.getBytes()), StandardCharsets.UTF_8);
        log.debug("SCHEMA_CONTROLLER::create $clazzName: {}", clazzName);
        return ResponseEntity.ok(itemService.create(clazzName, data));
    }
}