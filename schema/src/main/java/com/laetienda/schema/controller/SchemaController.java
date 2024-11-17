package com.laetienda.schema.controller;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.schema.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.Map;

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

    @PostMapping("${api.schema.login}")
    public ResponseEntity<String> login(Principal principal){
        log.debug("SCHEMA_CONTROLLER::login: {}", principal.getName());
        return ResponseEntity.ok("Succesfull log in by " + principal.getName());
    }

    @PostMapping("${api.schema.createPath}")
    public <T> ResponseEntity<T> create(@RequestParam(required = true) String clase, @RequestBody String data) throws NotValidCustomException{
        String clazzName = new String(Base64.getUrlDecoder().decode(clase.getBytes()), StandardCharsets.UTF_8);
        log.debug("SCHEMA_CONTROLLER::create $clazzName: {}", clazzName);
        try {
            Class<T> clazz = (Class<T>) Class.forName(clazzName);
            return ResponseEntity.ok(itemService.create(clazz, data));
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            log.trace(e.getMessage(), e);
            throw new NotValidCustomException(e.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }
    }

    @PostMapping("${api.schema.findPath}")
    public <T> ResponseEntity<T> find(@RequestParam String clase, @RequestBody Map<String, String> body) throws NotValidCustomException{
        String clazzName = new String(Base64.getUrlDecoder().decode(clase.getBytes()), StandardCharsets.UTF_8);
        log.debug("SCHEMA_CONTROLLER::find $clazzName: {}", clazzName);
        try {
            Class<T> clazz = (Class<T>) Class.forName(clazzName);
            return ResponseEntity.ok(itemService.find(clazz, body));
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            log.trace(e.getMessage(), e);
            throw new NotValidCustomException(e.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }
    }

    @PutMapping("${api.schema.updatePath}")
    public <T> ResponseEntity<T> update(@RequestParam String clase, @RequestBody String data) throws NotValidCustomException {
        String clazzName = new String(Base64.getUrlDecoder().decode(clase.getBytes()), StandardCharsets.UTF_8);
        log.debug("SCHEMA_CONTROLER::update $clazzName: {}", clazzName);

        try{
            Class<T> clazz = (Class<T>) Class.forName(clazzName);
            return ResponseEntity.ok(itemService.update(clazz, data));
        }catch (ClassNotFoundException e){
            log.error(e.getMessage());
            log.trace(e.getMessage(), e);
            throw new NotValidCustomException(e.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }
    }

    @PostMapping("${api.schema.deletePath}")
    public ResponseEntity<Boolean> delete(@RequestParam String clase, @RequestBody Map<String, String> body) throws NotValidCustomException{
        String clazzName = new String(Base64.getUrlDecoder().decode(clase.getBytes()), StandardCharsets.UTF_8);
        log.debug("SCHEMA_CONTROLLER::delete $clazzName: {}", clazzName);

        try {
            Class clazz = Class.forName(clazzName);
            itemService.delete(clazz, body);
            return ResponseEntity.ok(true);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            log.trace(e.getMessage(), e);
            throw new NotValidCustomException(e.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }
    }
}