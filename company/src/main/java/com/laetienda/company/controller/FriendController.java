package com.laetienda.company.controller;

import com.laetienda.company.service.FriendService;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Friend;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.company.folder}") //api/v0/company
public class FriendController {
    private final static Logger log = LoggerFactory.getLogger(FriendController.class);

    @Autowired private FriendService service;

    @GetMapping("${api.company.friend.find.file}")
    public ResponseEntity<Friend> find(@PathVariable String companyId, @PathVariable String memberUserId, @PathVariable String friendUserId) throws NotValidCustomException {
        log.info("FRIEND_CONTROLLER::find. $companyId: {} | $memberUserId: {}, $friendUserId {}", companyId, memberUserId, friendUserId);
        return ResponseEntity.ok(service.find(companyId, memberUserId, friendUserId));
    }

    @PutMapping("${api.company.friend.add.file}")
    public ResponseEntity<Friend> add(@PathVariable String companyId, @PathVariable String memberUserId, @PathVariable String friendUserId) throws NotValidCustomException {
        log.info("FRIEND_CONTROLLER::add. $companyId: {} | $memberUserId: {}, $friendUserId {}", companyId, memberUserId, friendUserId);
        return ResponseEntity.ok(service.add(companyId, memberUserId, friendUserId));
    }

    @PutMapping("${api.company.friend.update.file}")
    public ResponseEntity<Friend> update(@RequestBody @Valid Friend friend) throws NotValidCustomException{
        log.info("FRIEND_CONTROLLER::update. $friendId: {}", friend.getId());
        return ResponseEntity.ok(service.update(friend));
    }

    @DeleteMapping("${api.company.friend.delete.file}")
    public ResponseEntity<Void> delete(@PathVariable String companyId, @PathVariable String memberUserId, @PathVariable String friendUserId) throws NotValidCustomException {
        log.info("FRIEND_CONTROLLER::delete. $companyId: {} | $memberUserId: {}, $friendUserId {}", companyId, memberUserId, friendUserId);
        service.delete(companyId, memberUserId, friendUserId);
        return ResponseEntity.noContent().build();
    }
}
