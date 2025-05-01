package com.laetienda.usuerKc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("api/v0/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired private Environment env;

    @GetMapping("${api.usuario.test.file}") //api/v0/user/test.html
    public ResponseEntity<String> test(Principal principal){
        log.trace("USER_CONTROLLER::test. $api.usuario.test.file: {}", env.getProperty("api.usuario.test.file"));
        log.trace("USER_CONTROLLER::test $api.usuario.login.path: {}", env.getProperty("api.usuario.login.path"));

        if(principal instanceof OAuth2AuthenticationToken authentication){
            log.trace("USER_CONTROLLER::test $principal.name: {}", principal.getName());

        }else if(principal != null){
            log.trace("USER_CONTROLLER::test (non-OAuth2): {}", principal.getName());

        }else{
            log.trace("USER_CONTROLLER::test No authenticated user found");
        }

        return ResponseEntity.ok("Successful test.");
    }

    @GetMapping("${api.usuario.login.file}") //api/v0/user/login.html
    public ResponseEntity<String> login(Principal principal){
        log.trace("USER_CONTROLLER::login");
        log.trace("USER_CONTROLLER::login $user: {}", principal.getName());

        if(principal instanceof OAuth2AuthenticationToken authentication){
            authentication.getPrincipal().getAttribute("email");

        }else if(principal != null){
            log.trace("USER_CONTROLLER::login (non-OAuth2): {}", principal.toString());

        }else{
            log.trace("USER_CONTROLLER::login No authenticated user found");
        }

        return ResponseEntity.ok(String.format("Successful login. $user: %s", principal.getName()));
//        return ResponseEntity.ok("Successful login");
    }

    @GetMapping("${api.usuario.testAuthorization.file}")//api/v0/user/testAuthorization.html
    public ResponseEntity<String> testAuthorization(Principal principal){
        log.trace("USER_CONTROLLER::testAuthorization. $user: {}", principal.getName());
        return ResponseEntity.ok(String.format(
                "Successful authorization logging. $User: %s",
                principal.getName()));
    }
}
