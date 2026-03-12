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

    @GetMapping("${api.company.friend.file.find}")
    public ResponseEntity<Friend> find(@PathVariable String companyId, @PathVariable String userId) throws NotValidCustomException {
        log.info("FRIEND_CONTROLLER::find. $companyId: {} | $userId: {}", companyId, userId);
        return ResponseEntity.ok(service.find(companyId, userId));
    }

    @PutMapping("${api.company.friend.file.add}")
    public ResponseEntity<Friend> add(@PathVariable String companyId, @PathVariable String userId) throws NotValidCustomException {
        log.info("FRIEND_CONTROLLER::add. $companyId: {} | $userId {}", companyId, userId);
        return ResponseEntity.ok(service.add(companyId, userId));
    }

    @PutMapping("${api.company.friend.file.accept}")
    public ResponseEntity<Friend> accept(@PathVariable String companyId, @PathVariable String userId) throws NotValidCustomException{
        log.info("FRIEND_CONTROLLER::accept. $companyId: {} | $buddyUserId: {}", companyId, userId);
        return ResponseEntity.ok(service.accept(companyId, userId));
    }

    @PutMapping("${api.company.friend.file.block}")
    public ResponseEntity<Friend> block(@PathVariable String companyId, @PathVariable String userId) throws NotValidCustomException {
        log.info("FRIEND_CONTROLLER::block. $companyId: {} | $userId: {}", companyId, userId);
        return ResponseEntity.ok(service.block(companyId, userId));
    }

    @PutMapping("${api.company.friend.file.unblock}")
    public ResponseEntity<Friend> unblock(@PathVariable String companyId, @PathVariable String userId) throws NotValidCustomException {
        log.info("FRIEND_CONTROLLER::unblock. $companyId: {} | $userId: {}", companyId, userId);
        return ResponseEntity.ok(service.unblock(companyId, userId));
    }
}
