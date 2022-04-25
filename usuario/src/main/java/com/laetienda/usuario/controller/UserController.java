package com.laetienda.usuario.controller;

import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.model.Prueba;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v0/user")
public class UserController {

    private static final Logger log  = LoggerFactory.getLogger(UserController.class);

    @GetMapping({"/", "home", "home.html", "index", "index.html"})
    public String home(){
        return "Hello Word";
    }

    @PostMapping("add")
    public Usuario add(@Valid @RequestBody Usuario user){
        return user;
    }

    @PostMapping("test")
    public ResponseEntity test(@Valid @RequestBody Prueba prueba, BindingResult validation){
        ResponseEntity result = null;

        if(validation.hasErrors()){
            result = new ResponseEntity(HttpStatus.BAD_REQUEST);
            for(FieldError error : validation.getFieldErrors()){
                log.debug("{}: {}", error.getField(), error.getDefaultMessage());

            }
        }else{
            result = new ResponseEntity(HttpStatus.CREATED);
        }
        return result;
    }
}
