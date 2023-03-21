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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("api/v0/group")
public class GroupController {
    final private static Logger log = LoggerFactory.getLogger(GroupController.class);
    @Autowired
    private GroupService service;

    @Value("${spring.ldap.username}")
    private String ldapusername;

//    @GetMapping("groups.html")
//    public ResponseEntity<GroupList> findAll(){
//        log.trace("Running findAll groups controller");
//        return ResponseEntity.ok(service.findAll());
//    }

    @GetMapping("groups.html")
    public ResponseEntity<GroupList> findAllByMember(@RequestParam("user") String username) throws NotValidCustomException {

        if(username == null) {
            log.trace("Running findAll groups controller");
            return ResponseEntity.ok(service.findAll());
        }else {
            log.trace("Running findAllByMember group controller. $username: {}", username);
            return ResponseEntity.ok(service.findAllByMember(username));
        }
    }

    @GetMapping("group.html")
    public ResponseEntity<Group> findByName(@RequestParam String name) throws NotValidCustomException {
        return ResponseEntity.ok(service.findByName(name));
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

    @GetMapping("isMember.html")
    public ResponseEntity<Boolean> isMember(@RequestParam Map<String, String> params) throws NotValidCustomException {
        log.trace("Running group isMember controller");
        params.forEach((param, value) -> {
            log.trace("${}: {}", param, value);
        });

        return ResponseEntity.ok(service.isMember(params.get("group"), params.get("user")));
    }

    @PutMapping("addMember.html")
    public ResponseEntity<Group> addMember(@RequestParam Map<String, String> params) throws NotValidCustomException {
        log.trace("Running group addMember controller");
        params.forEach((param, value) -> {
            log.trace("${}: {}", param, value);
        });

        return ResponseEntity.ok(service.addMember(params.get("group"), params.get("user")));
    }

    @DeleteMapping("removeMember.html")
    public ResponseEntity<Group> removeMember(@RequestParam("user") String username, @RequestParam("group") String gname) throws NotValidCustomException {
        log.trace("Running group removeMember controller. $user: {}, $Group: {}", username, gname);
        return ResponseEntity.ok(service.removeMember(gname, username));
    }

    @PutMapping("addOwner.html")
    public ResponseEntity<Group> addOwner(@RequestParam("user") String username, @RequestParam("group") String gname) throws NotValidCustomException {
        log.trace("Running group add Owner controller. $user: {}, $Group: {}", username, gname);
        return ResponseEntity.ok(service.addOwner(gname, username));
    }

    @DeleteMapping("removeOwner.html")
    public ResponseEntity<Group> removeOwner(@RequestParam("user") String username, @RequestParam("group") String gname) throws NotValidCustomException {
        log.trace("Running group remove Owner controller. $user: {}, $Group: {}", username, gname);
        return ResponseEntity.ok(service.removeOwner(gname, username));
    }
}
