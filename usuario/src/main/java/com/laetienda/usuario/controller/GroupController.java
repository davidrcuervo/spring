package com.laetienda.usuario.controller;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.usuario.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v0/group")
public class GroupController {
    final private static Logger log = LoggerFactory.getLogger(GroupController.class);
    @Autowired
    private GroupService service;

    @Value("${spring.ldap.username}")
    private String ldapusername;

    @GetMapping("hola.html")
    public ResponseEntity<String> hola(){
        log.trace(ldapusername);
        return ResponseEntity.ok("Hello Group Word");
    }

    @GetMapping("groups.html")
    public ResponseEntity<GroupList> findAll(){
        return ResponseEntity.ok(new GroupList(service.findAll()));
    }

    @GetMapping("params.html")
    public ResponseEntity<String> testParameters(@RequestParam String name){
        log.trace("Running test parameters controller. $name: {}", name);
        return ResponseEntity.ok("Succesfully. $name: " + name);
    }

    @GetMapping("group.html")
    public ResponseEntity<Group> findByName(@RequestParam String name) throws NotValidCustomException {
        return ResponseEntity.ok(service.findGroupByName(name));
    }

    @PostMapping("create.html")
    public ResponseEntity<Group> create(@Valid @RequestBody Group group) throws NotValidCustomException {
        log.trace("Running group create controller. $Group: {}", group != null ? group.getName() : "null");
        return ResponseEntity.ok(service.create(group));
    }

    @PutMapping("update.html")
    public ResponseEntity<Group> update(@RequestParam String name, @Valid @RequestBody Group group) throws NotValidCustomException{
        log.trace("Running group update controller. $Group: {}", name);
        return ResponseEntity.ok(service.update(group, name));
    }

    @DeleteMapping("delete.html")
    public ResponseEntity<Boolean> delete(@RequestParam String name) throws NotValidCustomException {
        log.trace("Running group delete controller. $Group: {}", name);
        service.delete(name);
        return ResponseEntity.ok(true);
    }
}
