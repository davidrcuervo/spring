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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v0/user")
public class UserController {

    private static final Logger log  = LoggerFactory.getLogger(UserController.class);

    @GetMapping({"/", "home", "home.html", "index", "index.html"})
    public String home(){
        return "Hello Word";
    }

    @PostMapping("add")
    public ResponseEntity<Usuario> add(@Valid @RequestBody Usuario user){

        return ResponseEntity.ok(user);
    }

    @PostMapping("test")
    public ResponseEntity<Object> test(@Valid @RequestBody Prueba prueba){
        return ResponseEntity.ok(prueba);
    }
}
