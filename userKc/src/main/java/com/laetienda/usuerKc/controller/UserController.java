package com.laetienda.usuerKc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v0/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired private Environment env;

    @GetMapping("${api.usuario.test.file}") //api/v0/user/test.html
    public ResponseEntity<String> test(){
        log.trace("USER_CONTROLLER::test. $api.usuario.test.file: {}", env.getProperty("api.usuario.test.file"));
        log.trace("USER_CONTROLLER::test $api.usuario.login.path: {}", env.getProperty("api.usuario.login.path"));

        return ResponseEntity.ok("Successful test.");
    }

    @GetMapping("${api.usuario.login.file}") //api/v0/user/login.html
    public ResponseEntity<String> login(Authentication authentication){
        log.trace("USER_CONTROLLER::login");
//        log.trace("USER_CONTROLLER::login $user: {}", authentication.getName());
//        return ResponseEntity.ok(String.format("Successful login. $user: %s", authentication.getName()));
        return ResponseEntity.ok("Successful login");
    }
}
