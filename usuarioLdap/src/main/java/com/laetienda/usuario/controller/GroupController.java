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

    @GetMapping("findAll")
    public ResponseEntity<GroupList> findAll() throws NotValidCustomException{
        log.debug("GROUP_CONTROLLER::findAll.");
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("${api.group.findAllByMember}")
    public ResponseEntity<GroupList> findAllByMember(@PathVariable("username") String username) throws NotValidCustomException {
        log.trace("GROUP_CONTROLLER::findAllByMember. $username: {}", username);
        return ResponseEntity.ok(service.findAllByMember(username));
    }

    @GetMapping("findByName")
    public ResponseEntity<Group> findByName(@RequestParam String name) throws NotValidCustomException {
        log.trace("GROUP::Controler -> finding group by name. $gname: {}", name);
        return ResponseEntity.ok(service.findByName(name));
    }

    @PostMapping("create")
    public ResponseEntity<Group> create(@Valid @RequestBody Group group) throws NotValidCustomException {
        log.trace("GROUP_CONTROLLER::Create. $Group: {}", group != null ? group.getName() : "null");
        return ResponseEntity.ok(service.create(group));
    }

    @PutMapping("update")
    public ResponseEntity<Group> update(@RequestParam String name, @Valid @RequestBody Group group) throws NotValidCustomException{
        log.trace("GROUP_CONTROLLER::Update. $Group: {}", name);
        return ResponseEntity.ok(service.update(group, name));
    }

    @DeleteMapping("delete")
    public ResponseEntity<Boolean> delete(@RequestParam String name) throws NotValidCustomException {
        log.trace("GROUP_CONTROLLER::Delete. $GroupName: {}", name);
        service.delete(name);
        return ResponseEntity.ok(true);
    }

    @GetMapping("isMember")
    public ResponseEntity<Boolean> isMember(@RequestParam Map<String, String> params) throws NotValidCustomException {
        log.trace("GROUP_CONTROLLER::isMember");
        params.forEach((param, value) -> {
            log.trace("${}: {}", param, value);
        });

        return ResponseEntity.ok(service.isMember(params.get("group"), params.get("user")));
    }

    @GetMapping("isOwner")
    public ResponseEntity<Boolean> isOwner(@RequestParam Map<String, String> params) throws NotValidCustomException {
        log.trace("GROUP_CONTROLLER::isOwner");
        params.forEach((param, value) -> {
            log.trace("${}: {}", param, value);
        });

        return ResponseEntity.ok(service.isOwner(params.get("group"), params.get("user")));
    }

    @PutMapping("addMember")
    public ResponseEntity<Group> addMember(@RequestParam Map<String, String> params) throws NotValidCustomException {
        log.trace("GROP_CONTROLLER::AddMember");
        params.forEach((param, value) -> {
            log.trace("${}: {}", param, value);
        });

        return ResponseEntity.ok(service.addMember(params.get("group"), params.get("user")));
    }

    @DeleteMapping("removeMember.html")
    public ResponseEntity<Group> removeMember(@RequestParam("user") String username, @RequestParam("group") String gname) throws NotValidCustomException {
        log.trace("GROUP_CONTROLLER::removeMember. $username: {}, $groupname: {}", username, gname);
        return ResponseEntity.ok(service.removeMember(gname, username));
    }

    @PutMapping("addOwner.html")
    public ResponseEntity<Group> addOwner(@RequestParam("user") String username, @RequestParam("group") String gname) throws NotValidCustomException {
        log.trace("Running group add Owner controller. $user: {}, $Group: {}", username, gname);
        return ResponseEntity.ok(service.addOwner(gname, username));
    }

    @DeleteMapping("removeOwner.html")
    public ResponseEntity<Group> removeOwner(@RequestParam("user") String username, @RequestParam("group") String gname) throws NotValidCustomException {
        log.trace("GROUP_CONTROLLER::removeOwner. $username: {}, $roupname: {}", username, gname);
        return ResponseEntity.ok(service.removeOwner(gname, username));
    }

    @GetMapping("helloword.html")
    public ResponseEntity<String> helloWord(@RequestParam("username") String username){
        log.trace("Running hello word in group controller. group: {}", username);
        String result = String.format("Hello %s, Welcome to Group Controller.", username);
        return ResponseEntity.ok(result);
    }
}
