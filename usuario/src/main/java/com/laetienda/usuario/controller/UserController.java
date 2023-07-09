package com.laetienda.usuario.controller;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import com.laetienda.usuario.model.Prueba;
import com.laetienda.usuario.service.UserService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v0/user")
public class UserController {
    private static final Logger log  = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService service;

    @GetMapping({"/", "home", "home.html", "index", "index.html"})
    public String home(){
        return "Hello Word";
    }

    @GetMapping("users.html")
    public ResponseEntity<UsuarioList> findAll() throws NotValidCustomException {
        log.trace("Running findAll user controller");
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("user.html")
    public ResponseEntity<Usuario> getUser(@RequestParam String username) throws NotValidCustomException {
       log.trace("Find user by username. $username: {}", username);
       return ResponseEntity.ok(service.find(username));
    }

    @GetMapping("userByEmail.html")
    public List<Usuario> getUsersByEmail(@RequestParam String email){
        log.trace("$email: {}", email);
        return service.findByEmail(email);
    }

    @PostMapping("create.html")
    public ResponseEntity<Usuario> create(@Valid @RequestBody Usuario user) throws NotValidCustomException {
        log.trace("Creating user. $username: {}", user.getUsername());
        return ResponseEntity.ok(service.create(user));
    }

    @PostMapping("test")
    public ResponseEntity<Object> test(@Valid @RequestBody Prueba prueba){
        return ResponseEntity.ok(prueba);
    }

    @PutMapping("update.html")
    public ResponseEntity<Usuario> update(@Valid @RequestBody Usuario user) throws NotValidCustomException{
        log.trace("Running user update controller. $username: {}", user == null ? "empty" : user.getUsername());
        return ResponseEntity.ok(service.update(user));
    }

    @DeleteMapping("delete.html")
    public ResponseEntity<Boolean> delete(@RequestParam String username) throws NotValidCustomException {
        log.trace("delete user. $username: {}", username);
        service.delete(username);
        return ResponseEntity.ok(true);
    }

    @PostMapping("authenticate.html")
    public ResponseEntity<GroupList> authenticate(@RequestBody Usuario user) throws NotValidCustomException {
        log.trace("Running authenticate user controller. $username: {}", user == null ? "empty" : user.getUsername());
        return ResponseEntity.ok(service.authenticate(user));
    }

    @GetMapping("emailvalidation.html")
    public ResponseEntity<Boolean> emailValidation(@RequestParam(name="token") String encToken) throws NotValidCustomException{
        log.trace("Running emailvalidation user controller. $encToken: {}", encToken);
        service.emailValidation(encToken);
        return ResponseEntity.ok(true);
    }

    @PostMapping("requestpasswordrecovery.html")
    public ResponseEntity<String> requestPasswordRecovery(@RequestBody Map<String, String> params) throws NotValidCustomException{
        log.trace("Running requestPasswordRecovery user controller. $username: {}", params.get("username"));
        return ResponseEntity.ok(service.requestPasswordRecovery(params.get("username")));
    }

    @PostMapping("passwordrecovery.html")
    public ResponseEntity<Usuario> passwordRecovery(@RequestParam(name="token") String encToken, @RequestBody Map<String, String> parameters) throws NotValidCustomException{
//        throw new NotValidCustomException("Function not implemented", HttpStatus.INTERNAL_SERVER_ERROR, "user");
        log.trace("Running passwordRecovery user controller");

        for(Map.Entry<String, String> entry : parameters.entrySet()){
            log.trace("${}: {}", entry.getKey(), entry.getValue());
        }

        return ResponseEntity.ok(service.passwordRecovery(encToken, parameters));
    }
}
